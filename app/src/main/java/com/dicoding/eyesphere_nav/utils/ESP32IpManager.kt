package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.regex.Pattern

class ESP32IpManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: ESP32IpManager? = null
        private const val TAG = "ESP32IpManager"
        private const val PREFS_NAME = "esp32_config"
        private const val KEY_IP_ADDRESS = "ip_address"
        private const val DEFAULT_IP = "192.168.170.20"
        private const val DEFAULT_STREAM_PORT = "81"
        private const val DEFAULT_CONTROL_PORT = "80"
        
        // IP Address validation pattern
        private val IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        
        fun getInstance(): ESP32IpManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ESP32IpManager().also { INSTANCE = it }
            }
        }
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Get current ESP32 IP address
     */
    fun getIpAddress(context: Context): String {
        return getPreferences(context).getString(KEY_IP_ADDRESS, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    /**
     * Save ESP32 IP address
     */
    fun saveIpAddress(context: Context, ipAddress: String): Boolean {
        return try {
            if (!isValidIpAddress(ipAddress)) {
                Log.e(TAG, "Invalid IP address format: $ipAddress")
                return false
            }
            
            getPreferences(context)
                .edit()
                .putString(KEY_IP_ADDRESS, ipAddress)
                .apply()
            
            Log.d(TAG, "IP address saved: $ipAddress")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving IP address", e)
            false
        }
    }
    
    /**
     * Validate IP address format
     */
    fun isValidIpAddress(ipAddress: String): Boolean {
        return try {
            IP_PATTERN.matcher(ipAddress.trim()).matches()
        } catch (e: Exception) {
            Log.e(TAG, "Error validating IP address", e)
            false
        }
    }
    
    /**
     * Get ESP32 stream URL
     */
    fun getStreamUrl(context: Context): String {
        val ip = getIpAddress(context)
        return "http://$ip:$DEFAULT_STREAM_PORT/stream"
    }
    
    /**
     * Get ESP32 status URL for connection checking
     */
    fun getStatusUrl(context: Context): String {
        val ip = getIpAddress(context)
        return "http://$ip/status"
    }
    
    /**
     * Get ESP32 capture URL
     */
    fun getCaptureUrl(context: Context): String {
        val ip = getIpAddress(context)
        return "http://$ip/capture"
    }
    
    /**
     * Get ESP32 control URL
     */
    fun getControlUrl(context: Context): String {
        val ip = getIpAddress(context)
        return "http://$ip/control"
    }
    
    /**
     * Reset to default IP address
     */
    fun resetToDefault(context: Context): Boolean {
        return saveIpAddress(context, DEFAULT_IP)
    }
    
    /**
     * Get current configuration summary
     */
    fun getConfigSummary(context: Context): String {
        val ip = getIpAddress(context)
        return """
            ESP32 Configuration:
            IP Address: $ip
            Status URL: ${getStatusUrl(context)}
            Stream URL: ${getStreamUrl(context)}
            Capture URL: ${getCaptureUrl(context)}
            Control URL: ${getControlUrl(context)}
        """.trimIndent()
    }
}
