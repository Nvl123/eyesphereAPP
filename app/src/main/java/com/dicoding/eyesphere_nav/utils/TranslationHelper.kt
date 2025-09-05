package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TranslationHelper {
    
    private const val TAG = "TranslationHelper"
    
    /**
     * Translate server response to system language asynchronously
     * @param context Context to get current locale
     * @param indonesianText Server response in Indonesian
     * @param onTranslated Callback with translated text
     */
    fun translateServerResponseAsync(
        context: Context,
        indonesianText: String,
        onTranslated: (String) -> Unit
    ) {
        if (indonesianText.isBlank()) {
            onTranslated(indonesianText)
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val translatedText = withContext(Dispatchers.IO) {
                    TranslationService.getInstance().translateToSystemLanguage(indonesianText, context)
                }
                onTranslated(translatedText)
            } catch (e: Exception) {
                Log.e(TAG, "Translation failed", e)
                onTranslated(indonesianText) // Fallback to original text
            }
        }
    }
    
    /**
     * Translate server response to specific language asynchronously
     * @param indonesianText Server response in Indonesian
     * @param targetLanguage Target language code
     * @param onTranslated Callback with translated text
     */
    fun translateToLanguageAsync(
        indonesianText: String,
        targetLanguage: String,
        onTranslated: (String) -> Unit
    ) {
        if (indonesianText.isBlank()) {
            onTranslated(indonesianText)
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val translatedText = withContext(Dispatchers.IO) {
                    TranslationService.getInstance().translateToLanguage(indonesianText, targetLanguage)
                }
                onTranslated(translatedText)
            } catch (e: Exception) {
                Log.e(TAG, "Translation failed", e)
                onTranslated(indonesianText) // Fallback to original text
            }
        }
    }
    
    /**
     * Check if translation service is available
     */
    fun isTranslationAvailable(): Boolean {
        return TranslationService.getInstance().isAvailable()
    }
    
    /**
     * Get current system language code
     */
    fun getCurrentSystemLanguage(context: Context): String {
        return try {
            val currentLocale = context.resources.configuration.locales[0]
            currentLocale.language
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system language", e)
            "id" // Default to Indonesian
        }
    }
    
    /**
     * Check if current system language is Indonesian
     */
    fun isSystemLanguageIndonesian(context: Context): Boolean {
        return getCurrentSystemLanguage(context) == "id"
    }
    
    /**
     * Get language display name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "id" -> "Bahasa Indonesia"
            "en" -> "English"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "th" -> "ไทย"
            else -> languageCode
        }
    }
}
