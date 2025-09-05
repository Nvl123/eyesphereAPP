package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Helper class untuk mengelola efek getar di aplikasi EyeSphere Nav
 */
object VibrationHelper {
    
    private const val TAG = "VibrationHelper"
    
    /**
     * Efek getar singkat untuk feedback button click
     */
    fun vibrateShort(context: Context) {
        try {
            val vibrator = getVibrator(context)
            
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ - gunakan VibrationEffect
                    val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                    Log.d(TAG, "Short vibration triggered (VibrationEffect)")
                } else {
                    // Android < 8.0 - gunakan method lama
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                    Log.d(TAG, "Short vibration triggered (legacy)")
                }
            } else {
                Log.w(TAG, "Vibrator not available or doesn't support vibration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering short vibration", e)
        }
    }
    
    /**
     * Efek getar medium untuk feedback klasifikasi selesai
     */
    fun vibrateMedium(context: Context) {
        try {
            val vibrator = getVibrator(context)
            
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ - gunakan VibrationEffect dengan pola
                    val pattern = longArrayOf(0, 100, 100, 100, 100, 100) // Pola: diam, getar, diam, getar, diam, getar
                    val effect = VibrationEffect.createWaveform(pattern, -1) // -1 = tidak repeat
                    vibrator.vibrate(effect)
                    Log.d(TAG, "Medium vibration triggered (VibrationEffect)")
                } else {
                    // Android < 8.0 - gunakan method lama dengan pola
                    @Suppress("DEPRECATION")
                    val pattern = longArrayOf(0, 100, 100, 100, 100, 100)
                    vibrator.vibrate(pattern, -1) // -1 = tidak repeat
                    Log.d(TAG, "Medium vibration triggered (legacy)")
                }
            } else {
                Log.w(TAG, "Vibrator not available or doesn't support vibration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering medium vibration", e)
        }
    }
    
    /**
     * Efek getar panjang untuk feedback error
     */
    fun vibrateLong(context: Context) {
        try {
            val vibrator = getVibrator(context)
            
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ - gunakan VibrationEffect
                    val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                    Log.d(TAG, "Long vibration triggered (VibrationEffect)")
                } else {
                    // Android < 8.0 - gunakan method lama
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                    Log.d(TAG, "Long vibration triggered (legacy)")
                }
            } else {
                Log.w(TAG, "Vibrator not available or doesn't support vibration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering long vibration", e)
        }
    }
    
    /**
     * Efek getar custom dengan pola tertentu
     */
    fun vibratePattern(context: Context, pattern: LongArray, repeat: Int = -1) {
        try {
            val vibrator = getVibrator(context)
            
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ - gunakan VibrationEffect
                    val effect = VibrationEffect.createWaveform(pattern, repeat)
                    vibrator.vibrate(effect)
                    Log.d(TAG, "Pattern vibration triggered (VibrationEffect)")
                } else {
                    // Android < 8.0 - gunakan method lama
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, repeat)
                    Log.d(TAG, "Pattern vibration triggered (legacy)")
                }
            } else {
                Log.w(TAG, "Vibrator not available or doesn't support vibration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering pattern vibration", e)
        }
    }
    
    /**
     * Get vibrator instance berdasarkan versi Android
     */
    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - gunakan VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                // Android < 12 - gunakan Vibrator langsung
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vibrator instance", e)
            null
        }
    }
    
    /**
     * Cek apakah device mendukung vibration
     */
    fun hasVibrator(context: Context): Boolean {
        return try {
            val vibrator = getVibrator(context)
            vibrator?.hasVibrator() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking vibrator availability", e)
            false
        }
    }
}
