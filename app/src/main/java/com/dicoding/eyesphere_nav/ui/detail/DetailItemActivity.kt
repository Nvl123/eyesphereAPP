package com.dicoding.eyesphere_nav.ui.detail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dicoding.eyesphere_nav.data.database.ClassificationResult
import com.dicoding.eyesphere_nav.data.database.DatabaseHelper
import com.dicoding.eyesphere_nav.data.database.ProcessingStatus
import com.dicoding.eyesphere_nav.databinding.ActivityDetailItemBinding
import com.dicoding.eyesphere_nav.ml.ClassificationQueueService
import com.dicoding.eyesphere_nav.utils.TranslationHelper
import com.dicoding.eyesphere_nav.utils.ServerResponseProcessor
import com.dicoding.eyesphere_nav.utils.DetailActivityShimmerHelper
import com.facebook.shimmer.ShimmerFrameLayout
import java.io.File
import com.dicoding.eyesphere_nav.R

class DetailItemActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDetailItemBinding
    private lateinit var databaseHelper: DatabaseHelper
    private var currentResultId: Long = 0

    // Shimmer containers
    private lateinit var shimmerTitle: ShimmerFrameLayout
    private lateinit var shimmerDescription: ShimmerFrameLayout
    private lateinit var shimmerDate: ShimmerFrameLayout
    private lateinit var shimmerTime: ShimmerFrameLayout
    private lateinit var shimmerRecommendation: ShimmerFrameLayout

    
    companion object {
        const val EXTRA_CLASSIFICATION_RESULT = "classification_result"
        private const val TAG = "DetailItemActivity"
    }
    
    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                when (intent?.action) {
                    ClassificationQueueService.ACTION_PROGRESS_UPDATE -> {
                        val itemId = intent.getLongExtra(ClassificationQueueService.EXTRA_ITEM_ID, 0)
                        if (itemId == currentResultId) {
                            refreshItemData()
                        }
                    }
                    ClassificationQueueService.ACTION_ITEM_COMPLETED -> {
                        val itemId = intent.getLongExtra(ClassificationQueueService.EXTRA_ITEM_ID, 0)
                        if (itemId == currentResultId) {
                            refreshItemData()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in progress broadcast receiver", e)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityDetailItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        databaseHelper = DatabaseHelper(this)

        supportActionBar?.hide()

        registerProgressReceiver()
        
        // Initialize shimmer containers
        initializeShimmerContainers()
        
        // Start shimmer effect for translation simulation
        startShimmerEffect()
        
        // Get classification result data from intent (individual fields to avoid TransactionTooLargeException)
        val resultId = intent.getLongExtra("result_id", 0L)
        currentResultId = resultId
        
        if (resultId <= 0) {
            Log.e(TAG, "Invalid result ID")
            finish()
            return
        }
        
        // Load latest data from database
        refreshItemData()

        binding.topAppbar.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver)
            if (::databaseHelper.isInitialized) {
                databaseHelper.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    private fun registerProgressReceiver() {
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                progressReceiver,
                IntentFilter().apply {
                    addAction(ClassificationQueueService.ACTION_PROGRESS_UPDATE)
                    addAction(ClassificationQueueService.ACTION_ITEM_COMPLETED)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error registering progress receiver", e)
        }
    }
    
    /**
     * Initialize shimmer containers from layout
     */
    private fun initializeShimmerContainers() {
        try {
            // Access shimmer containers directly from binding
            shimmerTitle = binding.shimmerTitleContainer.shimmerTitle
            shimmerDescription = binding.shimmerDescriptionContainer.shimmerDescription
            shimmerDate = binding.shimmerDateContainer.shimmerDateTime
            shimmerTime = binding.shimmerTimeContainer.shimmerDateTime
            shimmerRecommendation = binding.shimmerRecommendationContainer.shimmerRecommendation
            
            Log.d(TAG, "Shimmer containers initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing shimmer containers: ${e.message}", e)
        }
    }
    
    /**
     * Start shimmer effect to simulate translation process
     */
    private fun startShimmerEffect() {
        try {
            Log.d(TAG, "Starting shimmer effect for translation simulation")
            
            // Start shimmer for all elements
            DetailActivityShimmerHelper.showTitleShimmer(binding.tvClassificationTitle, shimmerTitle)
            DetailActivityShimmerHelper.showDescriptionShimmer(binding.tvDescription, shimmerDescription)
            DetailActivityShimmerHelper.showDateShimmer(binding.tvDate, shimmerDate)
            DetailActivityShimmerHelper.showTimeShimmer(binding.tvTime, shimmerTime)
            
            // Always show shimmer for recommendation (since card is always visible)
            DetailActivityShimmerHelper.showRecommendationShimmer(binding.tvRecommendation, shimmerRecommendation)
            
            // Stop shimmer after 3 seconds (simulating translation completion)
            shimmerTitle.postDelayed({
                stopShimmerEffect()
            }, 3000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting shimmer effect: ${e.message}", e)
        }
    }
    
    /**
     * Stop shimmer effect and show actual content
     */
    private fun stopShimmerEffect() {
        try {
            Log.d(TAG, "Stopping shimmer effect and showing content")
            
            DetailActivityShimmerHelper.hideTitleShimmer(binding.tvClassificationTitle, shimmerTitle)
            DetailActivityShimmerHelper.hideDescriptionShimmer(binding.tvDescription, shimmerDescription)
            DetailActivityShimmerHelper.hideDateShimmer(binding.tvDate, shimmerDate)
            DetailActivityShimmerHelper.hideTimeShimmer(binding.tvTime, shimmerTime)
            
            // Always hide shimmer for recommendation
            DetailActivityShimmerHelper.hideRecommendationShimmer(binding.tvRecommendation, shimmerRecommendation)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping shimmer effect: ${e.message}", e)
        }
    }
    
    private fun refreshItemData() {
        try {
            val result = databaseHelper.getClassificationResultById(currentResultId)
            if (result != null) {
                displayClassificationResult(result)
            } else {
                Log.e(TAG, "Classification result not found for ID: $currentResultId")
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing item data", e)
        }
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.detail_hasil_analisis)
        }
    }
    
    private fun displayClassificationResult(result: ClassificationResult) {
        try {
            // Handle progress state
            when (result.status) {
                ProcessingStatus.PENDING, ProcessingStatus.PROCESSING -> {
                    showProgressUI(result)
                }
                ProcessingStatus.COMPLETED -> {
                    showCompletedUI(result)
                }
                ProcessingStatus.FAILED -> {
                    showFailedUI(result)
                }
            }
            
            // Always set basic info
            binding.tvDate.text = result.date
            binding.tvTime.text = result.time
            
            // Load and display image
            loadImageSafely(result.imagePath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying classification result", e)
        }
    }
    
    private fun showProgressUI(result: ClassificationResult) {
        // Show progress container
        binding.cvProgressContainer.visibility = View.VISIBLE
        
        // Update progress
        binding.progressBar.progress = result.progress
        binding.tvProgressText.text = "${result.progress}%"
        
        // Set progress title based on status
        binding.tvProgressTitle.text = when (result.status) {
            ProcessingStatus.PENDING -> getString(R.string.menunggu_antrian)
            ProcessingStatus.PROCESSING -> getString(R.string.memproses_analisis)
            else -> getString(R.string.memproses)
        }
        
        // Hide or dim completed UI
        binding.tvClassificationTitle.text = getString(R.string.sedang_diproses)
        binding.tvConfidence.text = getString(R.string.menunggu_hasil_analisis)
        binding.tvDescription.text = getString(R.string.gambar_sedang_dianalisis)
    }
    
    private fun showCompletedUI(result: ClassificationResult) {
        // Hide progress container
        binding.cvProgressContainer.visibility = View.GONE
        
        // Process classification title with automatic translation and text cleaning
        ServerResponseProcessor.processClassificationResult(
            context = this,
            indonesianClassification = result.classification,
            onProcessed = { processedClassification ->
                runOnUiThread {
                    // Use cleaned text for display
                    binding.tvClassificationTitle.text = processedClassification.cleanedClassification
                }
            },
            onError = { errorMessage ->
                Log.e(TAG, "Error processing classification: $errorMessage")
                // Fallback to original text
                runOnUiThread {
                    binding.tvClassificationTitle.text = result.classification
                }
            }
        )
        
        // Set confidence
        val confidencePercent = (result.confidence * 100).toInt()
        binding.tvConfidence.text = "Confidence: $confidencePercent%"
        
        // Process description with automatic translation and text cleaning
        val description = getClassificationDescription(result.classification)
        ServerResponseProcessor.processServerResponse(
            context = this,
            indonesianResponse = description,
            onProcessed = { processedDescription ->
                runOnUiThread {
                    binding.tvDescription.text = processedDescription.cleanedText
                }
            },
            onError = { errorMessage ->
                Log.e(TAG, "Error processing description: $errorMessage")
                // Fallback to original text
                runOnUiThread {
                    binding.tvDescription.text = description
                }
            }
        )
        
        // Show recommendation if available with automatic translation and text cleaning
        if (result.recommendation.isNotEmpty()) {
            binding.cvRecommendation.visibility = View.VISIBLE
            
            ServerResponseProcessor.processRecommendation(
                context = this,
                indonesianRecommendation = result.recommendation,
                onProcessed = { processedRecommendation ->
                    runOnUiThread {
                        // Use cleaned text for display
                        val cleanedRecommendation = processedRecommendation.cleanedRecommendation
                        
                        // Use HTML.fromHtml to render HTML tags (bold text, line breaks)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            binding.tvRecommendation.text = android.text.Html.fromHtml(cleanedRecommendation, android.text.Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            @Suppress("DEPRECATION")
                            binding.tvRecommendation.text = android.text.Html.fromHtml(cleanedRecommendation)
                        }
                    }
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error processing recommendation: $errorMessage")
                    // Fallback to original text
                    runOnUiThread {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            binding.tvRecommendation.text = android.text.Html.fromHtml(result.recommendation, android.text.Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            @Suppress("DEPRECATION")
                            binding.tvRecommendation.text = android.text.Html.fromHtml(result.recommendation)
                        }
                    }
                }
            )
        } else {
            binding.cvRecommendation.visibility = View.VISIBLE
            binding.tvRecommendation.text = "Tidak ada saran rekomendasi yang tersedia saat ini."
        }
        
        // Set confidence color based on value
        binding.tvConfidence.setTextColor(
            getColor(
                when {
                    result.confidence >= 0.8f -> android.R.color.holo_green_dark
                    result.confidence >= 0.6f -> android.R.color.holo_orange_dark
                    else -> android.R.color.holo_red_dark
                }
            )
        )
    }
    
    private fun showFailedUI(result: ClassificationResult) {
        // Hide progress container
        binding.cvProgressContainer.visibility = View.GONE
        
        // Show failed state
        binding.tvClassificationTitle.text = "Analisis Gagal"
        binding.tvConfidence.text = "Error: ${result.classification}"
        binding.tvDescription.text = "Terjadi kesalahan saat memproses gambar. Silakan coba lagi dengan mengambil foto baru."
        
        // Set error color
        binding.tvConfidence.setTextColor(getColor(android.R.color.holo_red_dark))
        binding.tvClassificationTitle.setTextColor(getColor(android.R.color.holo_red_dark))
    }
    
    private fun loadImageSafely(imagePath: String) {
        try {
            // Try multiple strategies to find the image
            val imageFile = when {
                // If it's just a filename, try to find it in common directories
                !imagePath.contains("/") -> {
                    val commonPaths = listOf(
                        File(getExternalFilesDir(null), imagePath),
                        File(filesDir, imagePath),
                        File("/storage/emulated/0/DCIM/Camera/", imagePath),
                        File("/storage/emulated/0/Pictures/", imagePath)
                    )
                    commonPaths.firstOrNull { it.exists() }
                }
                // If it's a full path
                else -> {
                    val file = File(imagePath)
                    if (file.exists()) file else null
                }
            }
            
            if (imageFile?.exists() == true) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    binding.ivClassificationImage.setImageBitmap(bitmap)
                    Log.d(TAG, "Image loaded successfully from: ${imageFile.absolutePath}")
                } else {
                    Log.w(TAG, "Could not decode image from: ${imageFile.absolutePath}")
                    setPlaceholderImage()
                }
            } else {
                Log.w(TAG, "Image file not found: $imagePath")
                setPlaceholderImage()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image: $imagePath", e)
            setPlaceholderImage()
        }
    }
    
    private fun setPlaceholderImage() {
        // Set a placeholder or default image
        try {
            binding.ivClassificationImage.setImageResource(android.R.drawable.ic_menu_gallery)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting placeholder image", e)
        }
    }
    
    private fun getClassificationDescription(classification: String): String {
        return when (classification.lowercase()) {
            "normal" -> "Mata Anda terlihat normal. Kondisi retina tidak menunjukkan tanda-tanda kelainan yang signifikan. Tetap jaga kesehatan mata dengan pemeriksaan rutin."
            
            "diabetic retinopathy", "diabetik retinopati" -> "Terdeteksi tanda-tanda retinopati diabetik. Kondisi ini dapat terjadi pada penderita diabetes dan memerlukan perhatian medis segera. Konsultasikan dengan dokter mata untuk penanganan lebih lanjut."
            
            "cataract", "katarak" -> "Terdeteksi tanda-tanda katarak. Kondisi ini menyebabkan lensa mata menjadi keruh dan dapat mengganggu penglihatan. Konsultasikan dengan dokter mata untuk evaluasi dan penanganan yang tepat."
            
            "glaucoma", "glaukoma" -> "Terdeteksi tanda-tanda glaukoma. Kondisi ini dapat menyebabkan kerusakan saraf optik dan kehilangan penglihatan permanen jika tidak ditangani. Segera konsultasikan dengan dokter mata."
            
            else -> "Hasil klasifikasi: $classification. Untuk informasi lebih detail dan diagnosis yang akurat, konsultasikan dengan dokter mata profesional."
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
