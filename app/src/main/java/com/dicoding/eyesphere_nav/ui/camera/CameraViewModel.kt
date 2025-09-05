package com.dicoding.eyesphere_nav.ui.camera

import android.content.Context
import android.provider.Settings.Global.getString

import android.util.Log
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.dicoding.eyesphere_nav.R

class CameraViewModel : ViewModel() {
    
    private val _brightnessValue = MutableLiveData<Float>()
    val brightnessValue: LiveData<Float> = _brightnessValue
    
    private val _brightnessStatus = MutableLiveData<String>()
    val brightnessStatus: LiveData<String> = _brightnessStatus
    
    private val _focusStatus = MutableLiveData<String>()
    val focusStatus: LiveData<String> = _focusStatus
    
    private val _isProcessingTouch = MutableLiveData<Boolean>()
    val isProcessingTouch: LiveData<Boolean> = _isProcessingTouch
    
    private val _cameraError = MutableLiveData<String?>()
    val cameraError: LiveData<String?> = _cameraError
    
    private val _photoResult = MutableLiveData<PhotoResult?>()
    val photoResult: LiveData<PhotoResult?> = _photoResult
    
    private val _isCameraReady = MutableLiveData<Boolean>()
    val isCameraReady: LiveData<Boolean> = _isCameraReady
    
    private val _imageSavedSuccessfully = MutableLiveData<Boolean>()
    val imageSavedSuccessfully: LiveData<Boolean> = _imageSavedSuccessfully
    
    // Camera related properties
    var cameraProvider: ProcessCameraProvider? = null
    var imageCapture: ImageCapture? = null
    var imageAnalyzer: ImageAnalysis? = null
    var camera: Camera? = null
    var lensFacing = CameraSelector.LENS_FACING_BACK
    var rotation = Surface.ROTATION_0
    
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    companion object {
        private const val TAG = "CameraViewModel"
    }
    
    init {
        _brightnessValue.value = 100f
        _brightnessStatus.value = "Bagus" // Will be updated with localized string when context is available
        _focusStatus.value = "Unknown"
        _isProcessingTouch.value = false
        _isCameraReady.value = false
        _imageSavedSuccessfully.value = false
    }
    
    /**
     * Initialize camera with given context and lifecycle owner
     */
    fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        try {
            Log.d(TAG, "Starting camera initialization...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            
            cameraProviderFuture.addListener({
                try {
                    Log.d(TAG, "Camera provider future listener triggered")
                    cameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "Camera provider obtained successfully")
                    bindCameraUseCases(context, lifecycleOwner, surfaceProvider)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get camera provider", e)
                    _cameraError.value = "Gagal mengakses kamera: ${e.message}"
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start camera initialization", e)
            _cameraError.value = "Gagal memulai kamera: ${e.message}"
        }
    }
    
    /**
     * Bind camera use cases
     */
    fun bindCameraUseCases(context: Context, lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        try {
            Log.d(TAG, "Starting to bind camera use cases...")
            val cameraProvider = this.cameraProvider ?: run {
                Log.e(TAG, "Camera provider is null")
                _cameraError.value = "Camera provider is null"
                return
            }
            
            Log.d(TAG, "Creating use cases...")
            // Create use cases
            val preview = CameraUtils.createPreview(rotation)
            Log.d(TAG, "Preview created, setting surface provider...")
            preview.setSurfaceProvider(surfaceProvider)
            
            imageCapture = CameraUtils.createImageCapture(rotation)
            Log.d(TAG, "Image capture created")
            
            imageAnalyzer = CameraUtils.createImageAnalysis(rotation).also { analysis ->
                analysis.setAnalyzer(cameraExecutor, CameraUtils.BrightnessAnalyzer { brightness ->
                    try {
                        if (!cameraExecutor.isShutdown) {
                            updateBrightness(brightness)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in brightness analyzer callback", e)
                    }
                })
            }
            Log.d(TAG, "Image analyzer created")
            
            // Create camera selector
            val cameraSelector = CameraUtils.createCameraSelector(lensFacing)
            Log.d(TAG, "Camera selector created for lens facing: $lensFacing")
            
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            Log.d(TAG, "Previous use cases unbound")
            
            // Bind use cases to camera
            Log.d(TAG, "Binding use cases to camera...")
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            Log.d(TAG, "Use cases bound to camera successfully")
            
            // Setup camera state observer
            camera?.cameraInfo?.cameraState?.observe(lifecycleOwner) { cameraState ->
                Log.d(TAG, "Camera state changed: ${cameraState.type}")
                handleCameraState(cameraState)
            }
            
            _isCameraReady.value = true
            Log.d(TAG, "Camera use cases bound successfully, camera is ready")
            
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            _cameraError.value = "Gagal mengkonfigurasi kamera: ${exc.message}"
        }
    }
    
    /**
     * Handle camera state changes
     */
    private fun handleCameraState(cameraState: CameraState) {
        when (cameraState.type) {
            CameraState.Type.PENDING_OPEN -> {
                Log.d(TAG, "Camera is pending open")
            }
            CameraState.Type.OPENING -> {
                Log.d(TAG, "Camera is opening")
            }
            CameraState.Type.OPEN -> {
                Log.d(TAG, "Camera is open and ready")
                // Camera is already ready since use cases are bound successfully
            }
            CameraState.Type.CLOSING -> {
                Log.d(TAG, "Camera is closing")
                _isCameraReady.value = false
            }
            CameraState.Type.CLOSED -> {
                Log.d(TAG, "Camera is closed")
                _isCameraReady.value = false
            }
        }
        
        cameraState.error?.let { error ->
            Log.e(TAG, "Camera error: ${error.code} - ${error.cause}")
            _cameraError.value = CameraUtils.getCameraErrorMessage(error.code)
            _isCameraReady.value = false
        }
    }
    
    /**
     * Update brightness value and status
     */
    private fun updateBrightness(brightness: Float) {
        try {
            val safeBrightness = CameraUtils.safeBrightnessValue(brightness)
            // Post values to ensure they're set on main thread
            _brightnessValue.postValue(safeBrightness)
            _brightnessStatus.postValue(CameraUtils.getBrightnessStatus(safeBrightness))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating brightness", e)
        }
    }
    
    /**
     * Update focus status
     */
    fun updateFocusStatus(status: String) {
        try {
            _focusStatus.postValue(status)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating focus status", e)
        }
    }
    
    /**
     * Set touch processing state
     */
    fun setTouchProcessing(isProcessing: Boolean) {
        try {
            _isProcessingTouch.postValue(isProcessing)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting touch processing state", e)
        }
    }
    
    /**
     * Perform focus action
     */
    fun performFocus(meteringPoint: MeteringPoint, context: Context) {
        try {
            val currentCamera = camera ?: run {
                Log.w(TAG, "Camera is null, cannot perform focus")
                setTouchProcessing(false)
                return
            }
            
            setTouchProcessing(true)
            updateFocusStatus("Focusing...")
            
            val focusMeteringAction = CameraUtils.createFocusMeteringAction(meteringPoint)
            val future = currentCamera.cameraControl.startFocusAndMetering(focusMeteringAction)
            
            future.addListener({
                try {
                    val result = future.get()
                    if (result.isFocusSuccessful) {
                        updateFocusStatus("Bagus")
                    } else {
                        updateFocusStatus("Gagal Focus")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Focus metering failed", e)
                    updateFocusStatus("Error")
                } finally {
                    setTouchProcessing(false)
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e(TAG, "Error in performFocus", e)
            setTouchProcessing(false)
        }
    }
    
    /**
     * Take photo
     */
    fun takePhoto(context: Context) {
        val imageCapture = imageCapture ?: run {
            _photoResult.value = PhotoResult.Error("Kamera belum siap")
            return
        }
        
        // Check conditions
        val currentBrightness = _brightnessValue.value ?: 100f
        val currentFocus = _focusStatus.value ?: "Unknown"
        
        if (!CameraUtils.isConditionGoodForPhoto(currentBrightness, currentFocus)) {
            val message = when {
                currentBrightness < 50 -> "Cahaya terlalu gelap untuk foto yang baik"
                currentBrightness > 200 -> "Cahaya terlalu terang untuk foto yang baik"
                else -> "Kondisi kurang optimal untuk foto"
            }
            _photoResult.value = PhotoResult.Warning(message)
            return
        }
        
        try {
            val outputOptions = CameraUtils.createImageOutputOptions(context)
            
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                        val errorMessage = CameraUtils.getImageCaptureErrorMessage(exception.imageCaptureError)
                        _photoResult.value = PhotoResult.Error(errorMessage)
                    }
                    
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri
                        Log.d(TAG, "Photo saved: $savedUri")
                        
                        // Process and save image immediately to database
                        savedUri?.let { uri ->
                            try {
                                // Resize image to 224x224 and save to internal storage
                                val fileName = com.dicoding.eyesphere_nav.utils.ImageUtils.generateProcessedImageName()
                                val processedImagePath = com.dicoding.eyesphere_nav.utils.ImageUtils.resizeAndSaveImage(context, uri, fileName)
                                
                                if (processedImagePath != null) {
                                    // Save to database immediately with PENDING status
                                    val classificationResult = com.dicoding.eyesphere_nav.data.database.ClassificationResult(
                                        imagePath = processedImagePath,
                                        classification = "Processing...",
                                        confidence = 0f,
                                        date = com.dicoding.eyesphere_nav.utils.DateTimeUtils.getCurrentDate(),
                                        time = com.dicoding.eyesphere_nav.utils.DateTimeUtils.getCurrentTime(),
                                        status = com.dicoding.eyesphere_nav.data.database.ProcessingStatus.PENDING,
                                        progress = 0
                                    )
                                    
                                    val databaseHelper = com.dicoding.eyesphere_nav.data.database.DatabaseHelper(context)
                                    val insertedId = databaseHelper.insertClassificationResult(classificationResult)
                                    databaseHelper.close()
                                    
                                                                            if (insertedId > 0) {
                                            Log.d(TAG, "Image saved to database with ID: $insertedId")
                                            // Start background processing queue
                                            Log.d(TAG, "Starting ClassificationQueueService")
                                            com.dicoding.eyesphere_nav.ml.ClassificationQueueService.startProcessing(context)
                                            _photoResult.postValue(PhotoResult.Success("Foto disimpan! Cek di History untuk melihat progress analisis."))
                                            
                                            // Signal that image was saved successfully
                                            _imageSavedSuccessfully.postValue(true)
                                            
                                            // Clean up old images
                                            com.dicoding.eyesphere_nav.utils.ImageUtils.cleanupOldImages(context)
                                        } else {
                                            Log.e(TAG, "Failed to save image to database")
                                            _photoResult.postValue(PhotoResult.Error("Gagal menyimpan ke database"))
                                        }
                                } else {
                                    _photoResult.postValue(PhotoResult.Error("Gagal memproses gambar"))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing and saving image", e)
                                _photoResult.postValue(PhotoResult.Error("Error: ${e.message}"))
                            }
                        } ?: run {
                            _photoResult.postValue(PhotoResult.Error("URI gambar tidak valid"))
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error taking photo", e)
            _photoResult.value = PhotoResult.Error("Gagal mengambil foto")
        }
    }
    
    /**
     * Flip camera (front/back)
     */
    fun flipCamera(context: Context, lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        try {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUseCases(context, lifecycleOwner, surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "Error flipping camera", e)
            _cameraError.value = "Gagal mengganti kamera"
        }
    }
    
    /**
     * Update rotation
     */
    fun updateRotation(newRotation: Int) {
        rotation = newRotation
        imageCapture?.targetRotation = rotation
    }
    
    /**
     * Get current brightness value
     */
    fun getCurrentBrightness(): Float {
        return _brightnessValue.value ?: 100f
    }
    
    /**
     * Get current focus status
     */
    fun getCurrentFocusStatus(): String {
        return _focusStatus.value ?: "Unknown"
    }
    
    /**
     * Update brightness status with localized string
     */
    fun updateBrightnessStatusWithContext(context: Context) {
        val brightness = _brightnessValue.value ?: 100f
        val status = when {
            brightness < 40 -> context.getString(R.string.s_gelap)
            brightness < 80 -> context.getString(R.string.gelap)
            brightness < 120 -> context.getString(R.string.s_terang)
            brightness < 160 -> context.getString(R.string.terang)
            else -> context.getString(R.string.bagus)
        }
        _brightnessStatus.value = status
    }
    
    /**
     * Clear camera error
     */
    fun clearError() {
        _cameraError.value = null
    }
    
    /**
     * Clear photo result
     */
    fun clearPhotoResult() {
        _photoResult.value = null
    }
    
    /**
     * Reset image saved successfully flag
     */
    fun resetImageSavedSuccessfully() {
        _imageSavedSuccessfully.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        try {
            Log.d(TAG, "ViewModel is being cleared, cleaning up resources...")
            
            // Clear camera references to avoid leaks
            camera = null
            imageCapture = null
            imageAnalyzer = null
            cameraProvider = null
            
            // Shutdown executor safely
            if (!cameraExecutor.isShutdown) {
                cameraExecutor.shutdown()
                Log.d(TAG, "Camera executor shutdown completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down camera executor", e)
        }
    }
    
    /**
     * Sealed class for photo results
     */
    sealed class PhotoResult {
        data class Success(val message: String) : PhotoResult()
        data class Error(val message: String) : PhotoResult()
        data class Warning(val message: String) : PhotoResult()
    }
}
