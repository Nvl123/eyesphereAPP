package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val TARGET_SIZE = 224
    
    /**
     * Resize image to 224x224 and save to internal storage
     * @param context Application context
     * @param sourceUri Original image URI
     * @param fileName Desired filename for the resized image
     * @return Path to the resized image file, or null if failed
     */
    fun resizeAndSaveImage(context: Context, sourceUri: Uri, fileName: String): String? {
        return try {
            Log.d(TAG, "Resizing image from URI: $sourceUri")
            
            // Load original bitmap
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI")
                return null
            }
            
            Log.d(TAG, "Original image size: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Resize to 224x224
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap, 
                TARGET_SIZE, 
                TARGET_SIZE, 
                true
            )
            
            // Save to internal storage
            val internalDir = File(context.filesDir, "processed_images")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            
            val outputFile = File(internalDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            
            val saved = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Clean up bitmaps
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            if (saved) {
                Log.d(TAG, "Image resized and saved to: ${outputFile.absolutePath}")
                return outputFile.absolutePath
            } else {
                Log.e(TAG, "Failed to save resized image")
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resizing and saving image", e)
            return null
        }
    }
    
    /**
     * Resize image from file path to 224x224
     * @param imagePath Original image file path
     * @param outputPath Desired output file path
     * @return True if successful, false otherwise
     */
    fun resizeImageFile(imagePath: String, outputPath: String): Boolean {
        return try {
            Log.d(TAG, "Resizing image from path: $imagePath")
            
            val originalBitmap = BitmapFactory.decodeFile(imagePath)
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from file: $imagePath")
                return false
            }
            
            Log.d(TAG, "Original image size: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Resize to 224x224
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap, 
                TARGET_SIZE, 
                TARGET_SIZE, 
                true
            )
            
            // Save resized image
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            
            val outputStream = FileOutputStream(outputFile)
            val saved = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Clean up bitmaps
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            if (saved) {
                Log.d(TAG, "Image resized and saved to: $outputPath")
            } else {
                Log.e(TAG, "Failed to save resized image")
            }
            
            return saved
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resizing image file", e)
            return false
        }
    }
    
    /**
     * Generate unique filename for processed image
     */
    fun generateProcessedImageName(): String {
        val timestamp = System.currentTimeMillis()
        return "processed_img_$timestamp.jpg"
    }
    
    /**
     * Get processed images directory
     */
    fun getProcessedImagesDir(context: Context): File {
        val dir = File(context.filesDir, "processed_images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Clean up old processed images (keep only last 50)
     */
    fun cleanupOldImages(context: Context) {
        try {
            val processedDir = getProcessedImagesDir(context)
            val files = processedDir.listFiles()?.sortedByDescending { it.lastModified() }
            
            files?.let { fileList ->
                if (fileList.size > 50) {
                    fileList.drop(50).forEach { file ->
                        if (file.delete()) {
                            Log.d(TAG, "Deleted old image: ${file.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old images", e)
        }
    }
}
