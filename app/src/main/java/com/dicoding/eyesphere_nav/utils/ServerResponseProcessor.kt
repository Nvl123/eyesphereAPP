package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dicoding.eyesphere_nav.utils.LanguageConsistencyHelper

/**
 * Service to process server responses with automatic translation and text cleaning
 * This ensures that only fresh server responses are translated, not existing database entries
 */
object ServerResponseProcessor {
    
    private const val TAG = "ServerResponseProcessor"
    
    /**
     * Process server response with automatic translation and text cleaning
     * Only translates if system language is not Indonesian
     * 
     * @param context Application context
     * @param indonesianResponse Original response from server in Indonesian
     * @param onProcessed Callback with processed result (translated + cleaned)
     * @param onError Callback for error handling
     */
    fun processServerResponse(
        context: Context,
        indonesianResponse: String,
        onProcessed: (ProcessedResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val targetLanguage = TranslationHelper.getCurrentSystemLanguage(context)
            
            // Check if translation is needed using consistency helper
            if (!LanguageConsistencyHelper.shouldTranslateServerResponse(context)) {
                // No translation needed, just clean the text
                val cleanedResponse = cleanResponseText(indonesianResponse)
                val processedResponse = ProcessedResponse(
                    originalText = indonesianResponse,
                    translatedText = indonesianResponse, // Same as original
                    cleanedText = cleanedResponse,
                    needsTranslation = false
                )
                onProcessed(processedResponse)
                return
            }
            
            // Check cache first for instant response
            val cachedTranslation = TranslationCache.getCachedTranslation(context, indonesianResponse, targetLanguage)
            if (cachedTranslation != null) {
                Log.d(TAG, "Using cached translation for: '$indonesianResponse'")
                val processedResponse = ProcessedResponse(
                    originalText = indonesianResponse,
                    translatedText = cachedTranslation.translatedText,
                    cleanedText = cachedTranslation.cleanedText,
                    needsTranslation = false // Already translated
                )
                onProcessed(processedResponse)
                return
            }
            
            // Translation needed and not in cache
            if (!TranslationHelper.isTranslationAvailable()) {
                // Translation service not available, use original with cleaning
                val cleanedResponse = cleanResponseText(indonesianResponse)
                val processedResponse = ProcessedResponse(
                    originalText = indonesianResponse,
                    translatedText = indonesianResponse, // Same as original
                    cleanedText = cleanedResponse,
                    needsTranslation = false
                )
                onProcessed(processedResponse)
                return
            }
            
            // Perform translation and cleaning
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    TranslationHelper.translateServerResponseAsync(
                        context, 
                        indonesianResponse
                    ) { translated ->
                        // Clean the translated text
                        val cleanedText = cleanResponseText(translated)
                        
                        // Cache the result for future use
                        if (TranslationCache.shouldCache(indonesianResponse)) {
                            TranslationCache.cacheTranslation(
                                context,
                                indonesianResponse,
                                translated,
                                cleanedText,
                                targetLanguage
                            )
                        }
                        
                        val processedResponse = ProcessedResponse(
                            originalText = indonesianResponse,
                            translatedText = translated,
                            cleanedText = cleanedText,
                            needsTranslation = true
                        )
                        
                        // Switch to main thread for callback
                        CoroutineScope(Dispatchers.Main).launch {
                            onProcessed(processedResponse)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing server response", e)
                    // Fallback to original with cleaning
                    val cleanedResponse = cleanResponseText(indonesianResponse)
                    val processedResponse = ProcessedResponse(
                        originalText = indonesianResponse,
                        translatedText = indonesianResponse, // Same as original
                        cleanedText = cleanedResponse,
                        needsTranslation = false
                    )
                    
                    withContext(Dispatchers.Main) {
                        onProcessed(processedResponse)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in processServerResponse", e)
            onError("Error processing response: ${e.message}")
        }
    }
    
    /**
     * Clean response text by removing markdown and formatting
     */
    private fun cleanResponseText(text: String): String {
        return try {
            TextCleaner.cleanMarkdown(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning response text", e)
            text // Return original if cleaning fails
        }
    }
    
    /**
     * Process classification result specifically
     */
    fun processClassificationResult(
        context: Context,
        indonesianClassification: String,
        onProcessed: (ProcessedClassification) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val targetLanguage = TranslationHelper.getCurrentSystemLanguage(context)
            
            // Check if translation is needed using consistency helper
            if (!LanguageConsistencyHelper.shouldTranslateServerResponse(context)) {
                // No translation needed, just clean the text
                val cleanedClassification = TextCleaner.cleanClassificationText(indonesianClassification)
                val processedClassification = ProcessedClassification(
                    originalClassification = indonesianClassification,
                    translatedClassification = indonesianClassification,
                    cleanedClassification = cleanedClassification,
                    needsTranslation = false
                )
                onProcessed(processedClassification)
                return
            }
            
            // Check cache first for instant response
            val cachedTranslation = TranslationCache.getCachedTranslation(context, indonesianClassification, targetLanguage)
            if (cachedTranslation != null) {
                Log.d(TAG, "Using cached classification translation for: '$indonesianClassification'")
                val processedClassification = ProcessedClassification(
                    originalClassification = indonesianClassification,
                    translatedClassification = cachedTranslation.translatedText,
                    cleanedClassification = cachedTranslation.cleanedText,
                    needsTranslation = false // Already translated
                )
                onProcessed(processedClassification)
                return
            }
            
            // Translation needed and not in cache
            if (!TranslationHelper.isTranslationAvailable()) {
                // Translation service not available, use original with cleaning
                val cleanedClassification = TextCleaner.cleanClassificationText(indonesianClassification)
                val processedClassification = ProcessedClassification(
                    originalClassification = indonesianClassification,
                    translatedClassification = indonesianClassification,
                    cleanedClassification = cleanedClassification,
                    needsTranslation = false
                )
                onProcessed(processedClassification)
                return
            }
            
            // Perform translation and cleaning
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    TranslationHelper.translateServerResponseAsync(
                        context, 
                        indonesianClassification
                    ) { translated ->
                        // Clean the translated classification
                        val cleanedClassification = TextCleaner.cleanClassificationText(translated)
                        
                        // Cache the result for future use
                        if (TranslationCache.shouldCache(indonesianClassification)) {
                            TranslationCache.cacheTranslation(
                                context,
                                indonesianClassification,
                                translated,
                                cleanedClassification,
                                targetLanguage
                            )
                        }
                        
                        val processedClassification = ProcessedClassification(
                            originalClassification = indonesianClassification,
                            translatedClassification = translated,
                            cleanedClassification = cleanedClassification,
                            needsTranslation = true
                        )
                        
                        // Switch to main thread for callback
                        CoroutineScope(Dispatchers.Main).launch {
                            onProcessed(processedClassification)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing classification result", e)
                    // Fallback to original with cleaning
                    val cleanedClassification = TextCleaner.cleanClassificationText(indonesianClassification)
                    val processedClassification = ProcessedClassification(
                        originalClassification = indonesianClassification,
                        translatedClassification = indonesianClassification,
                        cleanedClassification = cleanedClassification,
                        needsTranslation = false
                    )
                    
                    withContext(Dispatchers.Main) {
                        onProcessed(processedClassification)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in processClassificationResult", e)
            onError("Error processing classification: ${e.message}")
        }
    }
    
    /**
     * Process recommendation text specifically
     */
    fun processRecommendation(
        context: Context,
        indonesianRecommendation: String,
        onProcessed: (ProcessedRecommendation) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val targetLanguage = TranslationHelper.getCurrentSystemLanguage(context)
            
            // Check if translation is needed using consistency helper
            if (!LanguageConsistencyHelper.shouldTranslateServerResponse(context)) {
                // No translation needed, just clean the text
                val cleanedRecommendation = TextCleaner.cleanRecommendationText(indonesianRecommendation)
                val processedRecommendation = ProcessedRecommendation(
                    originalRecommendation = indonesianRecommendation,
                    translatedRecommendation = indonesianRecommendation,
                    cleanedRecommendation = cleanedRecommendation,
                    needsTranslation = false
                )
                onProcessed(processedRecommendation)
                return
            }
            
            // Check cache first for instant response
            val cachedTranslation = TranslationCache.getCachedTranslation(context, indonesianRecommendation, targetLanguage)
            if (cachedTranslation != null) {
                Log.d(TAG, "Using cached recommendation translation for: '$indonesianRecommendation'")
                val processedRecommendation = ProcessedRecommendation(
                    originalRecommendation = indonesianRecommendation,
                    translatedRecommendation = cachedTranslation.translatedText,
                    cleanedRecommendation = cachedTranslation.cleanedText,
                    needsTranslation = false // Already translated
                )
                onProcessed(processedRecommendation)
                return
            }
            
            // Translation needed and not in cache
            if (!TranslationHelper.isTranslationAvailable()) {
                // Translation service not available, use original with cleaning
                val cleanedRecommendation = TextCleaner.cleanRecommendationText(indonesianRecommendation)
                val processedRecommendation = ProcessedRecommendation(
                    originalRecommendation = indonesianRecommendation,
                    translatedRecommendation = indonesianRecommendation,
                    cleanedRecommendation = cleanedRecommendation,
                    needsTranslation = false
                )
                onProcessed(processedRecommendation)
                return
            }
            
            // Perform translation and cleaning
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    TranslationHelper.translateServerResponseAsync(
                        context, 
                        indonesianRecommendation
                    ) { translated ->
                        // Clean the translated recommendation
                        val cleanedRecommendation = TextCleaner.cleanRecommendationText(translated)
                        
                        // Cache the result for future use
                        if (TranslationCache.shouldCache(indonesianRecommendation)) {
                            TranslationCache.cacheTranslation(
                                context,
                                indonesianRecommendation,
                                translated,
                                cleanedRecommendation,
                                targetLanguage
                            )
                        }
                        
                        val processedRecommendation = ProcessedRecommendation(
                            originalRecommendation = indonesianRecommendation,
                            translatedRecommendation = translated,
                            cleanedRecommendation = cleanedRecommendation,
                            needsTranslation = true
                        )
                        
                        // Switch to main thread for callback
                        CoroutineScope(Dispatchers.Main).launch {
                            onProcessed(processedRecommendation)
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing recommendation", e)
                    // Fallback to original with cleaning
                    val cleanedRecommendation = TextCleaner.cleanRecommendationText(indonesianRecommendation)
                    val processedRecommendation = ProcessedRecommendation(
                        originalRecommendation = indonesianRecommendation,
                        translatedRecommendation = indonesianRecommendation,
                        cleanedRecommendation = cleanedRecommendation,
                        needsTranslation = false
                    )
                    
                    withContext(Dispatchers.Main) {
                        onProcessed(processedRecommendation)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in processRecommendation", e)
            onError("Error processing recommendation: ${e.message}")
        }
    }
    
    /**
     * Data class for processed server response
     */
    data class ProcessedResponse(
        val originalText: String,
        val translatedText: String,
        val cleanedText: String,
        val needsTranslation: Boolean
    )
    
    /**
     * Data class for processed classification result
     */
    data class ProcessedClassification(
        val originalClassification: String,
        val translatedClassification: String,
        val cleanedClassification: String,
        val needsTranslation: Boolean
    )
    
    /**
     * Data class for processed recommendation
     */
    data class ProcessedRecommendation(
        val originalRecommendation: String,
        val translatedRecommendation: String,
        val cleanedRecommendation: String,
        val needsTranslation: Boolean
    )
}
