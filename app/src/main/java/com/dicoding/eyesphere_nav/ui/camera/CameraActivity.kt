package com.dicoding.eyesphere_nav.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import com.dicoding.eyesphere_nav.R
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.camera.view.PreviewView
import com.dicoding.eyesphere_nav.databinding.ActivityCameraBinding
import com.dicoding.eyesphere_nav.ml.ServerClassificationService
import com.dicoding.eyesphere_nav.data.database.DatabaseHelper
import com.dicoding.eyesphere_nav.data.database.ClassificationResult
import com.dicoding.eyesphere_nav.data.database.ProcessingStatus
import com.dicoding.eyesphere_nav.utils.DateTimeUtils
import com.dicoding.eyesphere_nav.utils.VibrationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import com.dicoding.eyesphere_nav.utils.LanguageHelper
import com.dicoding.eyesphere_nav.utils.TranslationHelper
import com.dicoding.eyesphere_nav.utils.ServerResponseProcessor
import com.dicoding.eyesphere_nav.ui.dialog.ProcessingAnimationDialog
import com.dicoding.eyesphere_nav.utils.ESP32ConnectionManager
import com.google.android.material.slider.Slider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.dicoding.eyesphere_nav.utils.ESP32IpManager
import com.dicoding.eyesphere_nav.ui.dialog.ESP32IpConfigDialog


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel
    private lateinit var esp32ConnectionManager: ESP32ConnectionManager
    private lateinit var esp32IpManager: ESP32IpManager
    private var displayId: Int = -1
    
    // ESP32 stream variables
    private var streamJob: Job? = null
    private var isStreaming = false
    private var currentStreamBitmap: Bitmap? = null
    private var frameCount = 0

    // Orientation handling
    private var orientationEventListener: OrientationEventListener? = null

    // Display manager for orientation changes
    private val displayManager by lazy { getSystemService(DISPLAY_SERVICE) as DisplayManager }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            updateRotation()
        }
    }


    // ESP32 camera doesn't require device camera permissions
    // Only internet permission is needed (already declared in manifest)

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            processSelectedImage(selectedImageUri)
        }
    }

    // ESP32 camera doesn't need surface provider initialization
    private fun initializeCameraWithSurfaceProvider() {
        Log.d(TAG, "ESP32 camera mode - no surface provider needed")
        // This method is kept for compatibility but does nothing for ESP32
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide ActionBar - using buttons instead
        supportActionBar?.hide()

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        
        // Initialize ESP32 managers
        esp32ConnectionManager = ESP32ConnectionManager.getInstance()
        esp32IpManager = ESP32IpManager.getInstance()

        // Initialize display ID for orientation handling
        displayId = -1

        // Setup orientation listener
        setupOrientationListener()

        // Setup observers
        setupObservers()

        // Start ESP32 camera stream
        startESP32Stream()

        // Setup click listeners
        setupClickListeners()
        
        // Setup LED control
        setupLEDControl()
        
        // Setup touch listener for ESP32 stream view
        setupTouchFocus()
    }

    private fun setupOrientationListener() {
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val newRotation = CameraUtils.getRotationFromOrientation(orientation)

                if (viewModel.rotation != newRotation) {
                    viewModel.updateRotation(newRotation)
                }
            }
        }
    }

    private fun updateRotation() {
        // Handled by ViewModel
    }

    private fun setupObservers() {
        // Note: ESP32 camera handles brightness/focus analysis directly in analyzeFrame()
        // No need for viewModel observers since we're using ESP32 camera
        Log.d(TAG, "ESP32 camera mode - observers not needed")
    }

    private fun setupClickListeners() {
        binding.btnCapture.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            // Capture from ESP32 instead of device camera
            captureESP32Image()
        }

        binding.btnFlipCamera.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            // ESP32 camera doesn't support flip - show stream info instead
            showToast("ESP32 Camera - Frame: $frameCount")
        }

        binding.btnGalery.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            openGallery()
        }
        
        binding.btnIpConfig.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            showIpConfigDialog()
        }
    }



    private fun setupTouchFocus() {
        // ESP32 camera doesn't support touch focus, but we can setup touch to show stream info
        try {
            binding.esp32StreamView.setOnClickListener {
                // Show stream info or adjust settings
                showToast("Frame: $frameCount - Tap LED slider to adjust brightness")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up touch listener", e)
        }
    }



    private fun showToast(message: String) {
        try {
            // Check if translation service is available and message is in Indonesian
            if (TranslationHelper.isTranslationAvailable() && !TranslationHelper.isSystemLanguageIndonesian(this)) {
                // Translate message to system language asynchronously
                TranslationHelper.translateServerResponseAsync(this, message) { translatedMessage ->
                    // Show translated message in UI thread
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, translatedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Show original message if translation not needed or not available
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Fallback to original message if translation fails
            Log.e(TAG, "Error translating toast message", e)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Handle AI server response with automatic translation
     */
    /**
     * Handle AI response from server with automatic translation and text cleaning
     */
    private fun handleAIResponse(indonesianResponse: String) {
        try {
            // Process server response with automatic translation and text cleaning
            ServerResponseProcessor.processServerResponse(
                context = this,
                indonesianResponse = indonesianResponse,
                onProcessed = { processedResponse ->
                    Log.d(TAG, "Original AI response: ${processedResponse.originalText}")
                    Log.d(TAG, "Translated AI response: ${processedResponse.translatedText}")
                    Log.d(TAG, "Cleaned AI response: ${processedResponse.cleanedText}")
                    Log.d(TAG, "Needs translation: ${processedResponse.needsTranslation}")
                    
                    // Update UI with processed response
                    runOnUiThread {
                        // Use cleaned text for UI display
                        // You can update UI elements here with processedResponse.cleanedText
                        // For example: binding.tvClassificationResult.text = processedResponse.cleanedText
                        
                        // Store the processed response for database update
                        // This ensures we save the translated and cleaned version
                        updateDatabaseWithProcessedResponse(processedResponse)
                    }
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error processing AI response: $errorMessage")
                    // Fallback to original response
                    runOnUiThread {
                        // You can update UI elements here with indonesianResponse
                        // For example: binding.tvClassificationResult.text = indonesianResponse
                    }
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling AI response: ${e.message}", e)
            // Fallback to original response
            runOnUiThread {
                // You can update UI elements here with indonesianResponse
                // For example: binding.tvClassificationResult.text = indonesianResponse
            }
        }
    }
    
    /**
     * Update database with processed response (translated + cleaned)
     */
    private fun updateDatabaseWithProcessedResponse(processedResponse: ServerResponseProcessor.ProcessedResponse) {
        try {
            // Here you would update your database with the processed response
            // This ensures that the translated and cleaned text is stored
            // so users don't have to wait for translation when viewing history
            
            Log.d(TAG, "Updating database with processed response")
            Log.d(TAG, "Original: ${processedResponse.originalText}")
            Log.d(TAG, "Translated: ${processedResponse.translatedText}")
            Log.d(TAG, "Cleaned: ${processedResponse.cleanedText}")
            
            // TODO: Implement database update with processed response
            // Example:
            // databaseHelper.updateClassificationResult(
            //     itemId = currentItemId,
            //     classification = processedResponse.cleanedText,
            //     originalClassification = processedResponse.originalText,
            //     translatedClassification = processedResponse.translatedText,
            //     needsTranslation = processedResponse.needsTranslation
            // )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating database with processed response", e)
        }
    }

    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery", e)
            showToast(getString(R.string.gagal_membuka_galeri))
        }
    }

    private fun processSelectedImage(uri: Uri) {
        try {
            showToast(getString(R.string.memproses_gambar_dari_galeri))
            
            // Process image in background thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Copy image to app's internal storage
                    val imageFile = copyImageToInternalStorage(uri)
                    
                    if (imageFile != null) {
                        // Save to database first with PENDING status
                        val insertedId = saveImageToDatabase(imageFile.absolutePath)
                        
                        if (insertedId > 0) {
                            // Start classification process
                            startClassificationProcess(imageFile.absolutePath, insertedId)
                        } else {
                            withContext(Dispatchers.Main) {
                                showToast(getString(R.string.gagal_menyimpan_database))
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast(getString(R.string.gagal_memproses_gambar))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing selected image", e)
                    withContext(Dispatchers.Main) {
                        showToast(getString(R.string.error_generic, e.message ?: "Unknown error"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in processSelectedImage", e)
            showToast(getString(R.string.gagal_memproses_gambar))
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    // Create a unique filename
                    val timestamp = System.currentTimeMillis()
                    val filename = "gallery_image_$timestamp.jpg"
                    val file = File(filesDir, filename)
                    
                    // Copy the image
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    inputStream.close()
                    
                    Log.d(TAG, "Image copied to: ${file.absolutePath}")
                    file
                } else {
                    Log.e(TAG, "Failed to open input stream from URI")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error copying image to internal storage", e)
                null
            }
        }
    }

    private fun saveImageToDatabase(imagePath: String): Long {
        return try {
            val classificationResult = ClassificationResult(
                imagePath = imagePath,
                classification = getString(R.string.processing_status),
                confidence = 0f,
                date = DateTimeUtils.getCurrentDate(),
                time = DateTimeUtils.getCurrentTime(),
                status = ProcessingStatus.PENDING,
                progress = 0,
                recommendation = ""
            )
            
            val databaseHelper = DatabaseHelper(this@CameraActivity)
            val insertedId = databaseHelper.insertClassificationResult(classificationResult)
            databaseHelper.close()
            
            if (insertedId > 0) {
                Log.d(TAG, "Gallery image saved to database with ID: $insertedId")
                runOnUiThread {
                    // Show processing animation dialog instead of toast
                    showProcessingAnimationDialog()
                }
            } else {
                Log.e(TAG, "Failed to save gallery image to database")
            }
            
            insertedId
        } catch (e: Exception) {
            Log.e(TAG, "Error saving gallery image to database", e)
            -1
        }
    }

    private suspend fun startClassificationProcess(imagePath: String, itemId: Long) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting classification for gallery image: $imagePath with ID: $itemId")
                
                // Start classification using ServerClassificationService
                val result = ServerClassificationService.classifyImage(
                    context = this@CameraActivity,
                    itemId = itemId,
                    imagePath = imagePath,
                    onProgressUpdate = { progress ->
                        // Update progress in database and show toast
                        updateProgressInDatabase(itemId, progress)
                    }
                )
                
                // Classification completed successfully
                updateClassificationResultInDatabase(itemId, result.first, result.second, ProcessingStatus.COMPLETED)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in classification process", e)
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_klasifikasi, e.message ?: "Unknown error"))
                }
                
                // Update status to FAILED in database
                updateClassificationStatusInDatabase(itemId, ProcessingStatus.FAILED)
            }
        }
    }

    private fun updateProgressInDatabase(itemId: Long, progress: Int) {
        try {
            val databaseHelper = DatabaseHelper(this@CameraActivity)
            databaseHelper.updateClassificationProgress(itemId, progress, ProcessingStatus.PROCESSING)
            databaseHelper.close()
            Log.d(TAG, "Updated progress for item $itemId: $progress%")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress in database", e)
        }
    }

    private fun updateClassificationResultInDatabase(itemId: Long, classification: String, confidence: Float, status: ProcessingStatus) {
        try {
            // Process classification result with automatic translation and text cleaning
            ServerResponseProcessor.processClassificationResult(
                context = this@CameraActivity,
                indonesianClassification = classification,
                onProcessed = { processedClassification ->
                    Log.d(TAG, "Original classification: ${processedClassification.originalClassification}")
                    Log.d(TAG, "Translated classification: ${processedClassification.translatedClassification}")
                    Log.d(TAG, "Cleaned classification: ${processedClassification.cleanedClassification}")
                    Log.d(TAG, "Needs translation: ${processedClassification.needsTranslation}")
                    
                    // Update database with processed classification result
                    try {
                        val databaseHelper = DatabaseHelper(this@CameraActivity)
                        
                        // Store the cleaned (translated) text for display
                        // This ensures users see the translated and cleaned text immediately
                        databaseHelper.updateClassificationResult(itemId, processedClassification.cleanedClassification, confidence, status)
                        
                        // TODO: If you want to store both original and translated versions
                        // You can modify your database schema to include:
                        // - original_classification (Indonesian)
                        // - translated_classification (System language)
                        // - cleaned_classification (Final display text)
                        // - needs_translation (Boolean flag)
                        
                        databaseHelper.close()
                        Log.d(TAG, "Updated database with processed classification: ${processedClassification.cleanedClassification}")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating database with processed classification", e)
                    }
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error processing classification: $errorMessage")
                    // Fallback: store original classification
                    try {
                        val databaseHelper = DatabaseHelper(this@CameraActivity)
                        databaseHelper.updateClassificationResult(itemId, classification, confidence, status)
                        databaseHelper.close()
                        Log.d(TAG, "Fallback: stored original classification: $classification")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error storing fallback classification", e)
                    }
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating classification result in database", e)
            // Fallback: store original classification
            try {
                val databaseHelper = DatabaseHelper(this@CameraActivity)
                databaseHelper.updateClassificationResult(itemId, classification, confidence, status)
                databaseHelper.close()
                Log.d(TAG, "Fallback: stored original classification: $classification")
            } catch (e: Exception) {
                Log.e(TAG, "Error storing fallback classification", e)
            }
        }
    }

    private fun updateClassificationStatusInDatabase(itemId: Long, status: ProcessingStatus) {
        try {
            val databaseHelper = DatabaseHelper(this@CameraActivity)
            databaseHelper.updateClassificationProgress(itemId, 0, status)
            databaseHelper.close()
            Log.d(TAG, "Updated status for item $itemId: $status")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status in database", e)
        }
    }

    // ESP32 camera doesn't require runtime permissions
    private fun allPermissionsGranted() = true
    
    /**
     * Show processing GIF dialog and return to dashboard after completion
     */
    private fun showProcessingAnimationDialog() {
        val dialog = ProcessingAnimationDialog.newInstance(3000L) // 3 seconds display
        dialog.setOnAnimationCompleteListener {
            // Return to dashboard fragment
            navigateToHistory()
        }
        dialog.show(supportFragmentManager, "ProcessingAnimationDialog")
    }
    
    /**
     * Return to dashboard fragment after image processing
     */
    private fun navigateToHistory() {
        try {
            // Set result to indicate successful image processing
            setResult(RESULT_OK)
            
            // Finish activity to return to dashboard fragment
            Log.d(TAG, "Finishing CameraActivity to return to dashboard")
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing activity", e)
            // Fallback: finish activity
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
        displayManager.registerDisplayListener(displayListener, null)
        
        // Start ESP32 connection checking
        esp32ConnectionManager.startConnectionCheck(this)
        
        // Resume ESP32 stream if not already streaming
        if (!isStreaming) {
            startESP32Stream()
        }
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        displayManager.unregisterDisplayListener(displayListener)
        
        // Stop ESP32 connection checking
        esp32ConnectionManager.stopConnectionCheck()
        
        // Pause ESP32 stream
        stopESP32Stream()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.d(TAG, "CameraActivity is being destroyed")
            
            // Stop ESP32 stream
            stopESP32Stream()
            
            // Clear current bitmap
            currentStreamBitmap?.recycle()
            currentStreamBitmap = null
            
            // Remove orientation listener
            orientationEventListener?.disable()
            orientationEventListener = null
            
            Log.d(TAG, "CameraActivity cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during activity cleanup", e)
        }
    }
    
    // Menu removed - using button instead
    
    /**
     * Show IP configuration dialog
     */
    private fun showIpConfigDialog() {
        val dialog = ESP32IpConfigDialog.newInstance()
        dialog.setOnIpConfiguredListener(object : ESP32IpConfigDialog.OnIpConfiguredListener {
            override fun onIpConfigured(newIp: String) {
                // Restart stream with new IP
                restartStreamWithNewIp()
            }
        })
        dialog.show(supportFragmentManager, "ESP32IpConfigDialog")
    }
    
    /**
     * Restart ESP32 stream with new IP configuration
     */
    private fun restartStreamWithNewIp() {
        try {
            // Stop current stream
            stopESP32Stream()
            
            // Reset frame count and show connecting status
            frameCount = 0
            binding.tvStreamStatus.visibility = android.view.View.VISIBLE
            binding.tvStreamStatus.text = getString(R.string.esp32_stream_connecting)
            
            // Wait a moment then restart
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // Wait 1 second
                startESP32Stream()
                showToast("Reconnecting with new IP configuration...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting stream with new IP", e)
            showToast("Error restarting connection")
        }
    }
    
    /**
     * Setup LED control slider
     */
    private fun setupLEDControl() {
        try {
            // Initialize LED intensity text
            updateLEDIntensityText(0)
            
            // Setup slider listener
            binding.sliderLed.addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    val intensity = value.toInt()
                    updateLEDIntensityText(intensity)
                    
                    // Control LED intensity on ESP32
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val success = esp32ConnectionManager.controlLedIntensity(this@CameraActivity, intensity)
                            if (success) {
                                Log.d(TAG, "LED intensity set to: $intensity")
                            } else {
                                Log.w(TAG, "Failed to set LED intensity: $intensity")
                                withContext(Dispatchers.Main) {
                                    showToast("Failed to control LED")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error controlling LED", e)
                            withContext(Dispatchers.Main) {
                                showToast("Error controlling LED: ${e.message}")
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "LED control setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up LED control", e)
        }
    }
    
    /**
     * Update LED intensity text
     */
    private fun updateLEDIntensityText(intensity: Int) {
        try {
            binding.tvLedIntensity.text = getString(R.string.led_intensity, intensity)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating LED intensity text", e)
        }
    }
    
    /**
     * Start ESP32 camera stream
     */
    private fun startESP32Stream() {
        if (isStreaming) return
        
        // Show connecting status
        binding.tvStreamStatus.visibility = android.view.View.VISIBLE
        binding.tvStreamStatus.text = getString(R.string.esp32_stream_connecting)
        
        isStreaming = true
        frameCount = 0
        
        streamJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val streamUrl = esp32IpManager.getStreamUrl(this@CameraActivity)
                Log.d(TAG, "Starting ESP32 stream from: $streamUrl")
                
                val url = URL(streamUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 0 // No timeout for streaming
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = BufferedInputStream(connection.inputStream)
                    parseESP32MjpegStream(inputStream)
                } else {
                    withContext(Dispatchers.Main) {
                        showStreamError("Failed to connect: ${connection.responseCode}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting ESP32 stream: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showStreamError("Connection failed")
                }
            } finally {
                isStreaming = false
            }
        }
    }
    
    /**
     * Parse MJPEG stream from ESP32
     */
    private suspend fun parseESP32MjpegStream(input: InputStream) {
        val reader = input.bufferedReader()
        
        while (isStreaming && !Thread.currentThread().isInterrupted) {
            try {
                var line: String?
                var contentLength = -1

                // Read header until we find Content-Length
                while (isStreaming) {
                    line = reader.readLine() ?: break
                    if (line.startsWith("Content-Length", ignoreCase = true)) {
                        contentLength = line.split(":")[1].trim().toInt()
                        break
                    }
                }

                if (contentLength <= 0) continue

                // Skip empty line after header
                reader.readLine()

                // Read image data according to content-length
                val imageBytes = ByteArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength && isStreaming) {
                    val read = input.read(imageBytes, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }

                if (totalRead == contentLength) {
                    // Decode and display bitmap
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, totalRead)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            displayStreamFrame(bitmap)
                        }
                    } else {
                        Log.e(TAG, "Failed to decode frame")
                    }
                }

                // Skip boundary line
                reader.readLine()
                
            } catch (e: Exception) {
                if (isStreaming) {
                    Log.e(TAG, "Error parsing ESP32 stream: ${e.message}")
                }
                break
            }
        }
    }
    
    /**
     * Display stream frame and analyze brightness/focus
     */
    private fun displayStreamFrame(bitmap: Bitmap) {
        try {
            // Store current frame for capture
            currentStreamBitmap = bitmap
            
            // Display frame
            binding.esp32StreamView.setImageBitmap(bitmap)
            
            // Hide stream status overlay when first frame is received
            if (frameCount == 0) {
                binding.tvStreamStatus.visibility = android.view.View.GONE
                Log.d(TAG, "Stream connected - hiding status overlay")
            }
            
            // Update frame count
            frameCount++
            
            // Analyze frame for brightness and focus (every 10th frame to avoid performance issues)
            if (frameCount % 10 == 0) {
                analyzeFrame(bitmap)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying stream frame", e)
        }
    }
    
    /**
     * Analyze frame for brightness and focus
     */
    private fun analyzeFrame(bitmap: Bitmap) {
        try {
            // Analyze brightness
            val brightness = calculateBrightness(bitmap)
            val brightnessStatus = when {
                brightness < 50 -> getString(R.string.s_gelap)
                brightness < 100 -> getString(R.string.gelap)
                brightness < 150 -> getString(R.string.bagus)
                brightness < 200 -> getString(R.string.terang)
                else -> getString(R.string.s_terang)
            }
            
            // Analyze focus (simplified)
            val focusStatus = getString(R.string.bagus) // For now, always show "Good"
            
            // Update UI
            binding.tvBrightnessStatus.text = brightnessStatus
            binding.tvFocusStatus.text = focusStatus
            
            // Set text colors based on status
            val brightnessColor = when (brightnessStatus) {
                getString(R.string.bagus) -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
                else -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            }
            binding.tvBrightnessStatus.setTextColor(brightnessColor)
            binding.tvFocusStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing frame", e)
        }
    }
    
    /**
     * Calculate brightness of bitmap
     */
    private fun calculateBrightness(bitmap: Bitmap): Int {
        try {
            val width = bitmap.width.coerceAtMost(100) // Sample smaller area for performance
            val height = bitmap.height.coerceAtMost(100)
            
            var totalBrightness = 0
            var pixelCount = 0
            
            for (x in 0 until width step 5) {
                for (y in 0 until height step 5) {
                    val pixel = bitmap.getPixel(x, y)
                    val red = (pixel shr 16) and 0xFF
                    val green = (pixel shr 8) and 0xFF
                    val blue = pixel and 0xFF
                    
                    // Calculate luminance
                    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
                    totalBrightness += luminance
                    pixelCount++
                }
            }
            
            return if (pixelCount > 0) totalBrightness / pixelCount else 0
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating brightness", e)
            return 128 // Default middle brightness
        }
    }
    
    /**
     * Show stream error message and keep overlay visible
     */
    private fun showStreamError(message: String) {
        try {
            binding.tvStreamStatus.visibility = android.view.View.VISIBLE
            binding.tvStreamStatus.text = message
            showToast(message)
            Log.e(TAG, "ESP32 stream error: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing stream error", e)
        }
    }
    
    /**
     * Stop ESP32 stream
     */
    private fun stopESP32Stream() {
        isStreaming = false
        streamJob?.cancel()
        streamJob = null
        currentStreamBitmap = null
        Log.d(TAG, "ESP32 stream stopped")
    }
    
    /**
     * Capture image from ESP32 stream
     */
    private fun captureESP32Image() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First try to get image from current stream
                var bitmap = currentStreamBitmap
                
                // If no current frame, try to capture directly from ESP32
                if (bitmap == null) {
                    bitmap = captureDirectFromESP32()
                }
                
                if (bitmap != null) {
                    // Save bitmap to file
                    val imageFile = saveBitmapToFile(bitmap)
                    if (imageFile != null) {
                        // Save to database and start processing
                        val insertedId = saveImageToDatabase(imageFile.absolutePath)
                        if (insertedId > 0) {
                            withContext(Dispatchers.Main) {
                                // Show processing dialog
                                showProcessingAnimationDialog()
                            }
                            // Start classification process
                            startClassificationProcess(imageFile.absolutePath, insertedId)
                        } else {
                            withContext(Dispatchers.Main) {
                                showToast(getString(R.string.gagal_menyimpan_database))
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast(getString(R.string.error_penyimpanan_file))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Failed to capture image from ESP32")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing ESP32 image", e)
                withContext(Dispatchers.Main) {
                    showToast("Error capturing image: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Capture image directly from ESP32 capture endpoint
     */
    private suspend fun captureDirectFromESP32(): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val captureUrl = esp32IpManager.getCaptureUrl(this@CameraActivity)
                val url = URL(captureUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    connection.disconnect()
                    bitmap
                } else {
                    Log.e(TAG, "Failed to capture from ESP32, response code: ${connection.responseCode}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing directly from ESP32", e)
                null
            }
        }
    }
    
    /**
     * Save bitmap to file
     */
    private suspend fun saveBitmapToFile(bitmap: Bitmap): File? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val filename = "esp32_capture_$timestamp.jpg"
                val file = File(filesDir, filename)
                
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                }
                
                Log.d(TAG, "ESP32 image saved to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bitmap to file", e)
                null
            }
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
        // ESP32 camera doesn't require device camera permissions
        private val REQUIRED_PERMISSIONS = emptyArray<String>()
    }
}