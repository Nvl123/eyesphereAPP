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


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel
    private var displayId: Int = -1

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


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            initializeCameraWithSurfaceProvider()
        } else {
            showToast(getString(R.string.permission_kamera_diperlukan))
            finish()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            processSelectedImage(selectedImageUri)
        }
    }

    private fun initializeCameraWithSurfaceProvider() {
        Log.d(TAG, "Initializing camera with surface provider")
        try {
            viewModel.initializeCamera(this, this, binding.previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            showToast(getString(R.string.gagal_menginisialisasi_kamera))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]

        // Initialize display ID
        displayId = binding.previewView.display?.displayId ?: -1
        
        // Configure PreviewView
        binding.previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        Log.d(TAG, "PreviewView configured with FILL_CENTER scale type")

        // Setup orientation listener
        setupOrientationListener()

        // Setup observers
        setupObservers()

        // Check permissions
        if (allPermissionsGranted()) {
            initializeCameraWithSurfaceProvider()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Setup click listeners
        setupClickListeners()
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
        // Observe brightness status
        viewModel.brightnessStatus.observe(this) { status ->
            binding.tvBrightnessStatus.text = status
            val colorRes = CameraUtils.getBrightnessStatusColor(status)
            binding.tvBrightnessStatus.setTextColor(ContextCompat.getColor(this, colorRes))
        }

        // Observe focus status
        viewModel.focusStatus.observe(this) { status ->
            binding.tvFocusStatus.text = status
            val colorRes = CameraUtils.getFocusStatusColor(status)
            binding.tvFocusStatus.setTextColor(ContextCompat.getColor(this, colorRes))
        }

        // Observe camera errors
        viewModel.cameraError.observe(this) { error ->
            error?.let {
                Log.e(TAG, "Camera error observed: $it")
                showToast(it)
                // Handle fatal errors
                if (it.contains("fatal", ignoreCase = true)) {
                    Log.w(TAG, "Fatal camera error detected, recreating activity")
                    recreate()
                }
                viewModel.clearError()
            }
        }

        // Observe photo results
        viewModel.photoResult.observe(this) { result ->
            result?.let {
                when (it) {
                    is CameraViewModel.PhotoResult.Success -> {
                        showToast(it.message)
                    }
                    is CameraViewModel.PhotoResult.Error -> {
                        showToast(it.message)
                    }
                    is CameraViewModel.PhotoResult.Warning -> {
                        showToast(it.message)
                    }
                }
                viewModel.clearPhotoResult()
            }
        }

        // Observe camera ready state
        viewModel.isCameraReady.observe(this) { isReady ->
            if (isReady) {
                setupTouchFocus()
                Log.d(TAG, "Camera is ready and touch focus is set up")
            }
        }
        
        // Observe image saved successfully
        viewModel.imageSavedSuccessfully.observe(this) { success ->
            if (success) {
                showProcessingAnimationDialog()
                viewModel.resetImageSavedSuccessfully() // Reset using public method
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCapture.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            viewModel.takePhoto(this)
        }

        binding.btnFlipCamera.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            viewModel.flipCamera(this, this, binding.previewView.surfaceProvider)
        }

        binding.btnGalery.setOnClickListener {
            // Trigger vibration feedback
            VibrationHelper.vibrateShort(this)
            
            openGallery()
        }
    }



    private fun setupTouchFocus() {
        try {
            binding.previewView.setOnTouchListener { view, event ->
                try {
                    if (event.action == MotionEvent.ACTION_DOWN && !isDestroyed && !isFinishing) {
                        val isProcessing = viewModel.isProcessingTouch.value ?: false
                        if (!isProcessing) {
                            return@setOnTouchListener performFocusAction(event)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in touch listener", e)
                }
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up touch focus", e)
        }
    }

    private fun performFocusAction(event: MotionEvent): Boolean {
        return try {
            if (isDestroyed || isFinishing) {
                Log.w(TAG, "Activity is destroyed/finishing, skipping focus action")
                return false
            }
            
            val meteringPointFactory = binding.previewView.meteringPointFactory
            val meteringPoint = meteringPointFactory.createPoint(event.x, event.y)
            viewModel.performFocus(meteringPoint, this)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during touch focus", e)
            false
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
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
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.d(TAG, "CameraActivity is being destroyed")
            
            // Clear the surface provider to avoid memory leaks
            binding.previewView.setOnTouchListener(null)
            
            // Remove orientation listener
            orientationEventListener?.disable()
            orientationEventListener = null
            
            Log.d(TAG, "CameraActivity cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during activity cleanup", e)
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}