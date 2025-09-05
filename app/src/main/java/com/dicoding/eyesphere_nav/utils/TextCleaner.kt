package com.dicoding.eyesphere_nav.utils

import android.util.Log

/**
 * Utility class to clean and format text responses
 * Removes markdown formatting and cleans up text for better display
 */
object TextCleaner {
    
    private const val TAG = "TextCleaner"
    
    /**
     * Clean markdown formatting from text
     * Removes ** **, __ __, and other common markdown patterns
     */
    fun cleanMarkdown(text: String): String {
        return try {
            var cleanedText = text
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove **text** -> text
                .replace(Regex("__(.*?)__"), "$1") // Remove __text__ -> text
                .replace(Regex("\\*(.*?)\\*"), "$1") // Remove *text* -> text
                .replace(Regex("`(.*?)`"), "$1") // Remove `text` -> text
                .replace(Regex("~~(.*?)~~"), "$1") // Remove ~~text~~ -> text
                .replace(Regex("#{1,6}\\s"), "") // Remove # ## ### etc
                .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // Remove [text](url) -> text
                .replace(Regex("\\n\\s*\\n"), "\n\n") // Clean up multiple newlines
                .trim()
            
            Log.d(TAG, "Text cleaned: '$text' -> '$cleanedText'")
            cleanedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning markdown", e)
            text // Return original text if cleaning fails
        }
    }
    
    /**
     * Clean and format classification result text
     * Specifically for medical classification results
     */
    fun cleanClassificationText(text: String): String {
        return try {
            var cleanedText = cleanMarkdown(text)
            
            // Additional cleaning for medical text
            cleanedText = cleanedText
                .replace(Regex("\\b(?:Kondisi|Status|Hasil|Analisis):\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\b(?:Tidak ada|Tidak ditemukan|Normal|Baik|Sehat)\\b", RegexOption.IGNORE_CASE)) { matchResult ->
                    when (matchResult.value.lowercase()) {
                        "tidak ada" -> "Tidak ada"
                        "tidak ditemukan" -> "Tidak ditemukan"
                        "normal" -> "Normal"
                        "baik" -> "Baik"
                        "sehat" -> "Sehat"
                        else -> matchResult.value
                    }
                }
                .trim()
            
            Log.d(TAG, "Classification text cleaned: '$text' -> '$cleanedText'")
            cleanedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning classification text", e)
            text // Return original text if cleaning fails
        }
    }
    
    /**
     * Clean and format recommendation text
     * Specifically for medical recommendations
     */
    fun cleanRecommendationText(text: String): String {
        return try {
            var cleanedText = cleanMarkdown(text)
            
            // Additional cleaning for recommendation text
            cleanedText = cleanedText
                .replace(Regex("\\b(?:Rekomendasi|Saran|Saran medis):\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\b(?:Konsultasi|Periksa|Kunjungi)\\b", RegexOption.IGNORE_CASE)) { matchResult ->
                    when (matchResult.value.lowercase()) {
                        "konsultasi" -> "Konsultasi"
                        "periksa" -> "Periksa"
                        "kunjungi" -> "Kunjungi"
                        else -> matchResult.value
                    }
                }
                .trim()
            
            Log.d(TAG, "Recommendation text cleaned: '$text' -> '$cleanedText'")
            cleanedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning recommendation text", e)
            text // Return original text if cleaning fails
        }
    }
    
    /**
     * Check if text contains markdown formatting
     */
    fun containsMarkdown(text: String): Boolean {
        return text.contains("**") || text.contains("__") || text.contains("*") || 
               text.contains("`") || text.contains("~~") || text.contains("#") ||
               text.contains("[") && text.contains("](")
    }
}
