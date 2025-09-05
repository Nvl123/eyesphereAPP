package com.dicoding.eyesphere_nav.ml

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dicoding.eyesphere_nav.MainActivity
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.data.database.ClassificationResult
import com.dicoding.eyesphere_nav.data.database.DatabaseHelper
import com.dicoding.eyesphere_nav.data.database.ProcessingStatus
import com.dicoding.eyesphere_nav.utils.NotificationHelper
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class ClassificationQueueService : Service() {
    
    private var serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private lateinit var databaseHelper: DatabaseHelper
    private val isProcessing = AtomicBoolean(false)
    
    companion object {
        private const val TAG = "ClassificationQueueService"
        private const val NOTIFICATION_ID = 1003
        private const val CHANNEL_ID = "classification_queue_channel"
        
        const val ACTION_START_PROCESSING = "com.dicoding.eyesphere_nav.START_PROCESSING"
        const val ACTION_PROGRESS_UPDATE = "com.dicoding.eyesphere_nav.PROGRESS_UPDATE"
        const val ACTION_ITEM_COMPLETED = "com.dicoding.eyesphere_nav.ITEM_COMPLETED"
        
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_TOTAL = "total"
        
        fun startProcessing(context: Context) {
            Log.d(TAG, "startProcessing() called")
            val intent = Intent(context, ClassificationQueueService::class.java).apply {
                action = ACTION_START_PROCESSING
            }
            try {
                val serviceComponent = context.startService(intent)
                Log.d(TAG, "Service start result: $serviceComponent")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service", e)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        databaseHelper = DatabaseHelper(this)
        Log.d(TAG, "Server-based classification service initialized successfully")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_PROCESSING -> {
                Log.d(TAG, "ACTION_START_PROCESSING received, isProcessing: ${isProcessing.get()}")
                if (!isProcessing.get()) {
                    Log.d(TAG, "Starting processing queue...")
                    startProcessingQueue()
                } else {
                    Log.d(TAG, "Already processing, ignoring request")
                }
            }
            else -> {
                Log.w(TAG, "Unknown action received: ${intent?.action}")
            }
        }
        return START_STICKY // Keep service running to process queue
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }
    }
    
    private fun startProcessingQueue() {
        serviceScope.launch {
            isProcessing.set(true)
            
            try {
                Log.d(TAG, "Starting processing queue...")
                val pendingItems = databaseHelper.getPendingClassifications()
                Log.d(TAG, "Found ${pendingItems.size} pending classifications")
                
                if (pendingItems.isEmpty()) {
                    Log.d(TAG, "No pending items, stopping service")
                    stopSelf()
                    return@launch
                }
                
                // Server-based classification is always available
                Log.d(TAG, "Server-based classification service ready")
                
                try {
                    startForeground(NOTIFICATION_ID, createNotification(getString(R.string.memproses_gambar, pendingItems.size)))
                    Log.d(TAG, "Started foreground service notification")
                } catch (e: SecurityException) {
                    Log.w(TAG, "Cannot start foreground service, continuing in background", e)
                    // Continue processing without foreground service
                }
                
                pendingItems.forEachIndexed { index, item ->
                    try {
                        Log.d(TAG, "Processing item ${item.id} (${index + 1}/${pendingItems.size})")
                        processClassificationItem(item, index + 1, pendingItems.size)
                        
                        // Send progress update broadcast
                        sendProgressUpdate(item.id, index + 1, pendingItems.size)
                        
                        // Small delay between processing
                        delay(500)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing item ${item.id}", e)
                        databaseHelper.updateClassificationResult(
                            item.id, 
                            "Error: ${e.message ?: "Unknown error"}", 
                            0f, 
                            ProcessingStatus.FAILED
                        )
                    }
                }
                
                Log.d(TAG, "Queue processing completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in processing queue", e)
            } finally {
                isProcessing.set(false)
                Log.d(TAG, "Processing queue completed, isProcessing: ${isProcessing.get()}")
            }
        }
    }
    
    private suspend fun processClassificationItem(item: ClassificationResult, current: Int, total: Int) {
        Log.d(TAG, "Processing item ${item.id} ($current/$total)")
        
        // Update status to PROCESSING
        Log.d(TAG, "Updating item ${item.id} to PROCESSING status")
        databaseHelper.updateClassificationProgress(item.id, 0, ProcessingStatus.PROCESSING)
        
        try {
            // Simulate progress updates
            Log.d(TAG, "Starting progress simulation for item ${item.id}")
            for (progress in listOf(10, 30, 50, 70, 90)) {
                Log.d(TAG, "Updating progress for item ${item.id}: $progress%")
                databaseHelper.updateClassificationProgress(item.id, progress, ProcessingStatus.PROCESSING)
                delay(200) // Simulate processing time
            }
            
            Log.d(TAG, "Running classification for item ${item.id}")
            
            // Use server-based classification
            val (classification, confidence) = ServerClassificationService.classifyImage(
                this@ClassificationQueueService,
                item.id,
                item.imagePath
            ) { progress ->
                databaseHelper.updateClassificationProgress(item.id, progress, ProcessingStatus.PROCESSING)
            }
            
            Log.d(TAG, "Classification result for item ${item.id}: $classification ($confidence)")
            
            // Update with final result
            val updateSuccess = databaseHelper.updateClassificationResult(
                item.id, 
                classification, 
                confidence, 
                ProcessingStatus.COMPLETED
            )
            
            Log.d(TAG, "Database update success for item ${item.id}: $updateSuccess")
            
            // Send item completed broadcast
            sendItemCompleted(item.id)
            
            // Show notification for completed classification
            NotificationHelper.showClassificationCompletedNotification(
                this@ClassificationQueueService,
                item.id,
                classification,
                confidence
            )
            
            Log.d(TAG, "Item ${item.id} completed successfully: $classification ($confidence)")
            
            // Medical recommendation is already obtained from server during classification
            Log.d(TAG, "Medical recommendation obtained from server for item ${item.id}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing classification for item ${item.id}", e)
            
                                     val errorMessage = when {
                 e.message?.contains("Could not load image") == true -> 
                     getString(R.string.gambar_tidak_dapat_dimuat)
                 e.message?.contains("SSL") == true -> 
                     getString(R.string.masalah_keamanan_koneksi)
                 e.message?.contains("certificate") == true -> 
                     getString(R.string.masalah_sertifikat_server)
                 e.message?.contains("Server error") == true -> 
                     getString(R.string.server_tidak_dapat_diakses)
                 e.message?.contains("Network") == true -> 
                     getString(R.string.gagal_terhubung_server)
                 else -> getString(R.string.processing_failed, e.message ?: "Unknown error")
             }
            
            databaseHelper.updateClassificationResult(
                item.id, 
                errorMessage, 
                0f, 
                ProcessingStatus.FAILED
            )
            
            // Show notification for failed classification
            NotificationHelper.showClassificationFailedNotification(
                this@ClassificationQueueService,
                item.id,
                errorMessage
            )
            
            throw e
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.classification_queue),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.shows_progress)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.eyesphere_classification))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
    
    private fun sendProgressUpdate(itemId: Long, current: Int, total: Int) {
        val intent = Intent(ACTION_PROGRESS_UPDATE).apply {
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_PROGRESS, current)
            putExtra(EXTRA_TOTAL, total)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun sendItemCompleted(itemId: Long) {
        val intent = Intent(ACTION_ITEM_COMPLETED).apply {
            putExtra(EXTRA_ITEM_ID, itemId)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
