package com.dicoding.eyesphere_nav.ui.camera

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.hardware.display.DisplayManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dicoding.eyesphere_nav.R
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object CameraUtils {
    private const val TAG = "CameraUtils"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    /**
     * Check if photo conditions are good based on brightness and focus state
     */
    fun isConditionGoodForPhoto(brightness: Float, focusState: String): Boolean {
        return try {
            // Check if brightness is valid number and in good range
            val brightnessOk = !brightness.isNaN() &&
                    !brightness.isInfinite() &&
                    brightness in 40f..220f

            // Focus is considered good if not in error state
            val focusOk = focusState != "Error" && focusState != "Gagal Focus"

            brightnessOk && focusOk
        } catch (e: Exception) {
            Log.e(TAG, "Error checking photo conditions", e)
            false
        }
    }

    /**
     * Get brightness status text based on brightness value
     */
    fun getBrightnessStatus(brightness: Float): String {
        return try {
            val safeBrightness = when {
                brightness.isNaN() -> 0f
                brightness.isInfinite() -> 0f
                brightness < 0 -> 0f
                brightness > 255 -> 255f
                else -> brightness
            }

            when {
                safeBrightness < 40 -> "S. Gelap"
                safeBrightness < 60 -> "Gelap"
                safeBrightness > 220 -> "S.Terang"
                safeBrightness > 180 -> "Terang"
                else -> "Bagus"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting brightness status", e)
            "Error"
        }
    }

    /**
     * Get color resource for brightness status
     */
    fun getBrightnessStatusColor(status: String): Int {
        return when (status) {
            "Bagus" -> android.R.color.holo_green_light
            "Terang", "Gelap" -> android.R.color.holo_orange_light
            else -> android.R.color.holo_red_light
        }
    }

    /**
     * Get color resource for focus status
     */
    fun getFocusStatusColor(status: String): Int {
        return when (status) {
            "Bagus" -> android.R.color.holo_green_light
            "Focusing..." -> android.R.color.holo_blue_light
            "Gagal Focus", "Error" -> android.R.color.holo_red_light
            else -> android.R.color.white
        }
    }

    /**
     * Create rotation value based on orientation
     */
    fun getRotationFromOrientation(orientation: Int): Int {
        return when (orientation) {
            in 45..134 -> Surface.ROTATION_270
            in 135..224 -> Surface.ROTATION_180
            in 225..314 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
    }

    /**
     * Handle camera errors and return appropriate error message
     */
    fun getCameraErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            CameraState.ERROR_STREAM_CONFIG -> "Konfigurasi kamera gagal"
            CameraState.ERROR_CAMERA_IN_USE -> "Kamera sedang digunakan aplikasi lain"
            CameraState.ERROR_MAX_CAMERAS_IN_USE -> "Terlalu banyak kamera yang digunakan"
            CameraState.ERROR_CAMERA_DISABLED -> "Kamera dinonaktifkan oleh sistem"
            CameraState.ERROR_CAMERA_FATAL_ERROR -> "Error fatal pada kamera"
            else -> "Error kamera tidak dikenal"
        }
    }

    /**
     * Get error message for image capture errors
     */
    fun getImageCaptureErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            ImageCapture.ERROR_UNKNOWN -> "Error tidak dikenal"
            ImageCapture.ERROR_FILE_IO -> "Error penyimpanan file"
            ImageCapture.ERROR_CAPTURE_FAILED -> "Pengambilan foto gagal"
            ImageCapture.ERROR_CAMERA_CLOSED -> "Kamera tertutup"
            ImageCapture.ERROR_INVALID_CAMERA -> "Kamera tidak valid"
            else -> "Gagal mengambil foto"
        }
    }

    /**
     * Create output options for image capture
     */
    fun createImageOutputOptions(context: Context): ImageCapture.OutputFileOptions {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EyeSphere")
        }

        return ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
    }

    /**
     * Create focus metering action for touch focus
     */
    fun createFocusMeteringAction(meteringPoint: MeteringPoint): FocusMeteringAction {
        return FocusMeteringAction.Builder(meteringPoint)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Validate and safe-guard brightness value
     */
    fun safeBrightnessValue(brightness: Float): Float {
        return when {
            brightness.isNaN() || brightness.isInfinite() -> 100f
            brightness < 0f -> 0f
            brightness > 255f -> 255f
            else -> brightness
        }
    }

    /**
     * Create camera selector based on lens facing
     */
    fun createCameraSelector(lensFacing: Int): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    /**
     * Create preview use case
     */
    fun createPreview(rotation: Int): Preview {
        return Preview.Builder()
            .setTargetRotation(rotation)
            .build()
    }

    /**
     * Create image capture use case
     */
    fun createImageCapture(rotation: Int): ImageCapture {
        return ImageCapture.Builder()
            .setTargetRotation(rotation)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    /**
     * Create image analysis use case
     */
    fun createImageAnalysis(rotation: Int): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    /**
     * Get file path from URI
     */
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        return try {
            Log.d(TAG, "Getting file path from URI: $uri")
            
            // Try different approaches to get file path
            var filePath: String? = null
            
            // Method 1: Try MediaStore query
            try {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                        if (columnIndex >= 0) {
                            filePath = it.getString(columnIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore query failed", e)
            }
            
            // Method 2: If MediaStore fails, try to get real path from URI
            if (filePath.isNullOrEmpty()) {
                filePath = uri.path
            }
            
            Log.d(TAG, "Resolved file path: $filePath")
            
            // Validate file exists
            if (!filePath.isNullOrEmpty() && java.io.File(filePath).exists()) {
                filePath
            } else {
                Log.w(TAG, "File does not exist at path: $filePath")
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file path from URI", e)
            null
        }
    }

    /**
     * Brightness analyzer class for analyzing image brightness
     */
    class BrightnessAnalyzer(private val listener: (Float) -> Unit) : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            try {
                if (image.planes.isEmpty()) {
                    Log.w(TAG, "Image has no planes")
                    listener(100f)
                    return
                }
                
                val buffer = image.planes[0].buffer
                val data = buffer.toByteArray()

                if (data.isEmpty()) {
                    listener(100f) // Default safe value
                    return
                }

                val pixels = data.map { (it.toInt() and 0xFF).toDouble() }
                if (pixels.isEmpty()) {
                    listener(100f)
                    return
                }
                
                val brightness = pixels.average().toFloat()

                // Ensure brightness is a valid number
                val safeBrightness = safeBrightnessValue(brightness)
                listener(safeBrightness)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing brightness", e)
                try {
                    listener(100f) // Default safe value on error
                } catch (listenerException: Exception) {
                    Log.e(TAG, "Error calling listener", listenerException)
                }
            } finally {
                try {
                    image.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing image", e)
                }
            }
        }

        private fun ByteBuffer.toByteArray(): ByteArray {
            return try {
                rewind()
                val data = ByteArray(remaining())
                get(data)
                data
            } catch (e: Exception) {
                Log.e(TAG, "Error converting ByteBuffer to ByteArray", e)
                byteArrayOf() // Return empty array on error
            }
        }
    }
    
    /**
     * Get brightness status based on brightness value with localized strings
     * Requires context to access string resources
     */
    fun getBrightnessStatusWithContext(brightness: Float, context: android.content.Context): String {
        return try {
            // Handle NaN and infinite values
            val safeBrightness = when {
                brightness.isNaN() || brightness.isInfinite() -> 100f
                brightness < 0f -> 0f
                brightness > 255f -> 255f
                else -> brightness
            }

            when {
                safeBrightness < 40 -> context.getString(R.string.s_gelap)
                safeBrightness < 60 -> context.getString(R.string.gelap)
                safeBrightness > 220 -> context.getString(R.string.s_terang)
                safeBrightness > 180 -> context.getString(R.string.terang)
                else -> context.getString(R.string.bagus)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting brightness status", e)
            context.getString(R.string.error)
        }
    }
    
    /**
     * Get camera error message with localized strings
     * Requires context to access string resources
     */
    fun getCameraErrorMessageWithContext(errorCode: Int, context: android.content.Context): String {
        return when (errorCode) {
            CameraState.ERROR_STREAM_CONFIG -> context.getString(R.string.konfigurasi_kamera_gagal)
            CameraState.ERROR_CAMERA_IN_USE -> context.getString(R.string.kamera_sedang_digunakan)
            CameraState.ERROR_MAX_CAMERAS_IN_USE -> context.getString(R.string.terlalu_banyak_kamera)
            CameraState.ERROR_CAMERA_DISABLED -> context.getString(R.string.kamera_dinonaktifkan)
            CameraState.ERROR_CAMERA_FATAL_ERROR -> context.getString(R.string.error_fatal_kamera)
            else -> context.getString(R.string.error_kamera_tidak_dikenal)
        }
    }
    
    /**
     * Get image capture error message with localized strings
     * Requires context to access string resources
     */
    fun getImageCaptureErrorMessageWithContext(errorCode: Int, context: android.content.Context): String {
        return when (errorCode) {
            ImageCapture.ERROR_UNKNOWN -> context.getString(R.string.error_tidak_dikenal)
            ImageCapture.ERROR_FILE_IO -> context.getString(R.string.error_penyimpanan_file)
            ImageCapture.ERROR_CAPTURE_FAILED -> context.getString(R.string.pengambilan_foto_gagal)
            ImageCapture.ERROR_CAMERA_CLOSED -> context.getString(R.string.kamera_tertutup)
            ImageCapture.ERROR_INVALID_CAMERA -> context.getString(R.string.kamera_tidak_valid)
            else -> context.getString(R.string.gagal_mengambil_foto)
        }
    }
}
