package com.dicoding.eyesphere_nav.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dicoding.eyesphere_nav.MainActivity
import com.dicoding.eyesphere_nav.R

/**
 * Helper class untuk mengelola notification di aplikasi EyeSphere Nav
 */
object NotificationHelper {
    
    private const val TAG = "NotificationHelper"
    private const val CLASSIFICATION_CHANNEL_ID = "classification_completed_channel"
    private const val CLASSIFICATION_NOTIFICATION_ID = 2001
    
    /**
     * Buat notification channel untuk klasifikasi selesai
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel untuk klasifikasi selesai
            val classificationChannel = NotificationChannel(
                CLASSIFICATION_CHANNEL_ID,
                context.getString(R.string.classification_completed_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.classification_completed_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250) // Pola getar: diam, getar, diam, getar
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(classificationChannel)
            Log.d(TAG, "Notification channels created successfully")
        }
    }
    
    /**
     * Tampilkan notification ketika klasifikasi selesai
     */
    fun showClassificationCompletedNotification(
        context: Context,
        itemId: Long,
        classification: String,
        confidence: Float
    ) {
        try {
            Log.d(TAG, "Showing classification completed notification for item $itemId")
            
            // Intent untuk membuka MainActivity ketika notification diklik
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_history", true) // Extra untuk membuka History fragment
                putExtra("item_id", itemId) // Extra untuk highlight item tertentu
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                itemId.toInt(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Format confidence percentage
            val confidencePercent = String.format("%.1f%%", confidence * 100)
            
            // Build notification
            val notification = NotificationCompat.Builder(context, CLASSIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.classification_completed_title))
                .setContentText(context.getString(R.string.classification_completed_text, classification, confidencePercent))
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.classification_completed_big_text, classification, confidencePercent)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true) // Notification hilang ketika diklik
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 250, 250)) // Pola getar
                .setDefaults(NotificationCompat.DEFAULT_SOUND) // Suara default
                .build()
            
            // Show notification
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(CLASSIFICATION_NOTIFICATION_ID + itemId.toInt(), notification)
                    Log.d(TAG, "Notification shown successfully for item $itemId")
                } else {
                    Log.w(TAG, "Notifications are disabled by user")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing classification completed notification", e)
        }
    }
    
    /**
     * Tampilkan notification ketika klasifikasi gagal
     */
    fun showClassificationFailedNotification(
        context: Context,
        itemId: Long,
        errorMessage: String
    ) {
        try {
            Log.d(TAG, "Showing classification failed notification for item $itemId")
            
            // Intent untuk membuka MainActivity ketika notification diklik
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_history", true) // Extra untuk membuka History fragment
                putExtra("item_id", itemId) // Extra untuk highlight item tertentu
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                itemId.toInt(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Build notification
            val notification = NotificationCompat.Builder(context, CLASSIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.classification_failed_title))
                .setContentText(context.getString(R.string.classification_failed_text))
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.classification_failed_big_text, errorMessage)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setAutoCancel(true) // Notification hilang ketika diklik
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 100, 100, 100, 100, 100)) // Pola getar lebih pendek untuk error
                .setDefaults(NotificationCompat.DEFAULT_SOUND) // Suara default
                .build()
            
            // Show notification
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(CLASSIFICATION_NOTIFICATION_ID + itemId.toInt(), notification)
                    Log.d(TAG, "Failed notification shown successfully for item $itemId")
                } else {
                    Log.w(TAG, "Notifications are disabled by user")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing classification failed notification", e)
        }
    }
    
    /**
     * Cek apakah notification permission sudah diberikan (untuk Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // Untuk Android < 13, notification permission otomatis granted
        }
    }
}

