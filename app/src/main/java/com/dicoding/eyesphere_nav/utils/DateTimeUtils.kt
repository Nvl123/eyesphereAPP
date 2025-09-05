package com.dicoding.eyesphere_nav.utils

import java.text.SimpleDateFormat
import java.util.*
import com.dicoding.eyesphere_nav.R

object DateTimeUtils {
    
    private const val DATE_FORMAT = "dd MMMM yyyy"
    private const val TIME_FORMAT = "HH:mm:ss"
    private const val DATETIME_FORMAT = "dd MMMM yyyy, HH:mm:ss"
    
    /**
     * Get current date in format: dd MMMM yyyy (e.g., 22 Agustus 2025)
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale("id", "ID"))
        return dateFormat.format(Date())
    }
    
    /**
     * Get current time in 24-hour format: HH:mm:ss (e.g., 15:30:45)
     */
    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return timeFormat.format(Date())
    }
    
    /**
     * Get current date and time combined
     */
    fun getCurrentDateTime(): String {
        val dateTimeFormat = SimpleDateFormat(DATETIME_FORMAT, Locale("id", "ID"))
        return dateTimeFormat.format(Date())
    }
    
    /**
     * Format timestamp to date string
     */
    fun formatTimestampToDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale("id", "ID"))
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Format timestamp to time string
     */
    fun formatTimestampToTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * Format timestamp to datetime string
     */
    fun formatTimestampToDateTime(timestamp: Long): String {
        val dateTimeFormat = SimpleDateFormat(DATETIME_FORMAT, Locale("id", "ID"))
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * Get time difference in human readable format
     * Note: This method returns placeholder text. Use getTimeAgoWithContext() for localized strings.
     */
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000L -> "Baru saja"
            diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)} menit yang lalu"
            diff < 24 * 60 * 60 * 1000L -> "${diff / (60 * 60 * 1000L)} jam yang lalu"
            diff < 7 * 24 * 60 * 60 * 1000L -> "${diff / (24 * 60 * 60 * 1000L)} hari yang lalu"
            else -> formatTimestampToDate(timestamp)
        }
    }
    
    /**
     * Get time difference in human readable format with localized strings
     * Requires context to access string resources
     */
    fun getTimeAgoWithContext(timestamp: Long, context: android.content.Context): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000L -> context.getString(R.string.baru_saja)
            diff < 60 * 60 * 1000L -> context.getString(R.string.menit_yang_lalu, (diff / (60 * 1000L)).toInt())
            diff < 24 * 60 * 60 * 1000L -> context.getString(R.string.jam_yang_lalu, (diff / (60 * 60 * 1000L)).toInt())
            diff < 7 * 24 * 60 * 60 * 1000L -> context.getString(R.string.hari_yang_lalu, (diff / (24 * 60 * 60 * 1000L)).toInt())
            else -> formatTimestampToDate(timestamp)
        }
    }
}
