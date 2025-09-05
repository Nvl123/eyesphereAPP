package com.dicoding.eyesphere_nav.data.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClassificationResult(
    val id: Long = 0,
    val imagePath: String,
    val classification: String = "Processing...",
    val confidence: Float = 0f,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: ProcessingStatus = ProcessingStatus.PENDING,
    val progress: Int = 0, // 0-100
    val recommendation: String = "" // Gemini AI recommendation
) : Parcelable

enum class ProcessingStatus {
    PENDING,     // Waiting to be processed
    PROCESSING,  // Currently being processed
    COMPLETED,   // Processing completed successfully
    FAILED       // Processing failed
}
