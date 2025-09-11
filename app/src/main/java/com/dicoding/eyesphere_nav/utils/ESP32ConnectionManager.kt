package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

class ESP32ConnectionManager private constructor() {
    
    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        CONNECTING,
        FAILED
    }
    
    private val _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus
    
    private var checkJob: Job? = null
    private var isChecking = false
    private lateinit var esp32IpManager: ESP32IpManager
    
    companion object {
        @Volatile
        private var INSTANCE: ESP32ConnectionManager? = null
        private const val TAG = "ESP32ConnectionManager"
        private const val CONNECTION_TIMEOUT = 3000 // 3 seconds
        private const val CHECK_INTERVAL = 5000L // 5 seconds
        
        fun getInstance(): ESP32ConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ESP32ConnectionManager().also { INSTANCE = it }
            }
        }
    }
    
    init {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        esp32IpManager = ESP32IpManager.getInstance()
    }
    
    /**
     * Start checking ESP32 connection periodically
     */
    fun startConnectionCheck(context: Context) {
        Log.d(TAG, "========== STARTING ESP32 CONNECTION MONITORING ==========")
        
        if (isChecking) {
            Log.d(TAG, "Connection checking already running, skipping...")
            return
        }
        
        val currentIP = esp32IpManager.getIpAddress(context)
        val statusUrl = esp32IpManager.getStatusUrl(context)
        val streamUrl = esp32IpManager.getStreamUrl(context)
        
        Log.d(TAG, "ESP32 IP Address: $currentIP")
        Log.d(TAG, "Status URL: $statusUrl")
        Log.d(TAG, "Stream URL: $streamUrl")
        Log.d(TAG, "Check interval: ${CHECK_INTERVAL}ms")
        Log.d(TAG, "Connection timeout: ${CONNECTION_TIMEOUT}ms")
        
        isChecking = true
        checkJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Connection check coroutine started")
            
            while (isChecking) {
                try {
                    Log.d(TAG, "Performing periodic connection check...")
                    checkESP32Connection(context)
                    Log.d(TAG, "Connection check completed, waiting ${CHECK_INTERVAL}ms")
                    delay(CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in connection check loop", e)
                    Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "Exception message: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _connectionStatus.value = ConnectionStatus.FAILED
                    }
                    delay(CHECK_INTERVAL)
                }
            }
            
            Log.d(TAG, "Connection check coroutine ended")
        }
        
        Log.d(TAG, "ESP32 connection monitoring started successfully")
        Log.d(TAG, "========================================================")
    }
    
    /**
     * Stop checking ESP32 connection
     */
    fun stopConnectionCheck() {
        Log.d(TAG, "========== STOPPING ESP32 CONNECTION MONITORING ==========")
        Log.d(TAG, "Was checking: $isChecking")
        Log.d(TAG, "Job active: ${checkJob?.isActive}")
        
        isChecking = false
        checkJob?.cancel()
        checkJob = null
        
        Log.d(TAG, "ESP32 connection monitoring stopped")
        Log.d(TAG, "=========================================================")
    }
    
    /**
     * Check ESP32 connection once
     */
    suspend fun checkESP32Connection(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========== ESP32 CONNECTION CHECK START ==========")
                
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = ConnectionStatus.CONNECTING
                }
                
                val statusUrl = esp32IpManager.getStatusUrl(context)
                Log.d(TAG, "Attempting to connect to: $statusUrl")
                Log.d(TAG, "Connection timeout: ${CONNECTION_TIMEOUT}ms")
                
                val url = URL(statusUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.readTimeout = CONNECTION_TIMEOUT
                connection.requestMethod = "GET" // Use GET to get JSON status response
                
                Log.d(TAG, "HTTP request method: GET")
                Log.d(TAG, "Starting connection...")
                
                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage
                
                Log.d(TAG, "Response code: $responseCode")
                Log.d(TAG, "Response message: $responseMessage")
                
                // Read JSON response to validate ESP32 status
                val isConnected = if (responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        val inputStream = connection.inputStream
                        val response = inputStream.bufferedReader().use { it.readText() }
                        Log.d(TAG, "ESP32 Status Response: $response")
                        
                        // Check if response contains ESP32 status JSON
                        val isValidResponse = response.contains("led_intensity") && 
                                            response.contains("framesize") && 
                                            response.contains("quality")
                        
                        Log.d(TAG, "Valid ESP32 JSON response: $isValidResponse")
                        isValidResponse
                    } catch (e: Exception) {
                        Log.w(TAG, "Error reading status response: ${e.message}")
                        false
                    }
                } else {
                    false
                }
                
                connection.disconnect()
                
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = if (isConnected) {
                        ConnectionStatus.CONNECTED
                    } else {
                        ConnectionStatus.FAILED
                    }
                }
                
                Log.d(TAG, "Connection result: ${if (isConnected) "SUCCESS" else "FAILED"}")
                Log.d(TAG, "Status updated to: ${_connectionStatus.value}")
                Log.d(TAG, "========== ESP32 CONNECTION CHECK END ==========")
                
                isConnected
                
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "======== ESP32 CONNECTION TIMEOUT ========")
                Log.w(TAG, "Status URL: ${esp32IpManager.getStatusUrl(context)}")
                Log.w(TAG, "Timeout: ${CONNECTION_TIMEOUT}ms")
                Log.w(TAG, "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = ConnectionStatus.FAILED
                }
                false
            } catch (e: ConnectException) {
                Log.w(TAG, "======== ESP32 CONNECTION REFUSED ========")
                Log.w(TAG, "Status URL: ${esp32IpManager.getStatusUrl(context)}")
                Log.w(TAG, "Error: ${e.message}")
                Log.w(TAG, "Possible causes: ESP32 not powered, wrong IP, firewall")
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
                false
            } catch (e: UnknownHostException) {
                Log.w(TAG, "======== UNKNOWN HOST ========")
                Log.w(TAG, "Status URL: ${esp32IpManager.getStatusUrl(context)}")
                Log.w(TAG, "Error: ${e.message}")
                Log.w(TAG, "Possible causes: Wrong IP address, DNS issues")
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = ConnectionStatus.FAILED
                }
                false
            } catch (e: Exception) {
                Log.e(TAG, "======== ESP32 CONNECTION ERROR ========")
                Log.e(TAG, "Status URL: ${esp32IpManager.getStatusUrl(context)}")
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                Log.e(TAG, "Stack trace: ${e.stackTrace.contentToString()}")
                withContext(Dispatchers.Main) {
                    _connectionStatus.value = ConnectionStatus.FAILED
                }
                false
            }
        }
    }
    
    /**
     * Control LED intensity on ESP32
     */
    suspend fun controlLedIntensity(context: Context, intensity: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val controlUrl = esp32IpManager.getControlUrl(context)
                val url = URL("$controlUrl?var=led_intensity&val=$intensity")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.readTimeout = CONNECTION_TIMEOUT
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                connection.disconnect()
                
                val success = responseCode == HttpURLConnection.HTTP_OK
                Log.d(TAG, "LED control result: $success (intensity: $intensity)")
                success
                
            } catch (e: Exception) {
                Log.e(TAG, "Error controlling LED", e)
                false
            }
        }
    }
    
    /**
     * Get current connection status synchronously
     */
    fun getCurrentStatus(): ConnectionStatus {
        return _connectionStatus.value ?: ConnectionStatus.DISCONNECTED
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return getCurrentStatus() == ConnectionStatus.CONNECTED
    }
    
    /**
     * Get connection status string for UI
     */
    fun getConnectionStatusString(context: Context): String {
        return when (getCurrentStatus()) {
            ConnectionStatus.CONNECTED -> context.getString(com.dicoding.eyesphere_nav.R.string.status_koneksi)
            ConnectionStatus.DISCONNECTED -> context.getString(com.dicoding.eyesphere_nav.R.string.status_tidak_terkoneksi)
            ConnectionStatus.CONNECTING -> context.getString(com.dicoding.eyesphere_nav.R.string.status_menghubungkan)
            ConnectionStatus.FAILED -> context.getString(com.dicoding.eyesphere_nav.R.string.status_koneksi_gagal)
        }
    }
    
    /**
     * Get connection status color for UI
     */
    fun getConnectionStatusColor(context: Context): Int {
        return when (getCurrentStatus()) {
            ConnectionStatus.CONNECTED -> androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_green_dark)
            ConnectionStatus.CONNECTING -> androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            ConnectionStatus.DISCONNECTED, ConnectionStatus.FAILED -> androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_red_dark)
        }
    }
}
