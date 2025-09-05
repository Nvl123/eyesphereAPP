package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class TranslationService private constructor() {
    
    companion object {
        private const val TAG = "TranslationService"
        private const val GEMINI_API_KEY = "AIzaSyBav6iv6VFCtEBB57V4uFBdM2cyokje-gY"
        private const val MODEL_NAME = "gemini-2.0-flash"
        
        @Volatile
        private var INSTANCE: TranslationService? = null
        
        fun getInstance(): TranslationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationService().also { INSTANCE = it }
            }
        }
    }
    
    private var generativeModel: GenerativeModel? = null
    
    init {
        try {
            generativeModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = GEMINI_API_KEY,
                generationConfig = generationConfig {
                    temperature = 0.1f
                    topK = 1
                    topP = 0.8f
                    maxOutputTokens = 1024
                }
            )
            Log.d(TAG, "Gemini model initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Gemini model", e)
        }
    }
    
    /**
     * Translate text from Indonesian to target language
     * @param indonesianText Text in Indonesian language
     * @param targetLanguage Target language code (e.g., "en", "ja", "ko", "th")
     * @return Translated text or original text if translation fails
     */
    suspend fun translateToLanguage(
        indonesianText: String,
        targetLanguage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (targetLanguage == "id" || indonesianText.isBlank()) {
                return@withContext indonesianText
            }
            
            val model = generativeModel
            if (model == null) {
                Log.w(TAG, "Gemini model not initialized, returning original text")
                return@withContext indonesianText
            }
            
            val prompt = buildTranslationPrompt(indonesianText, targetLanguage)
            Log.d(TAG, "Translation prompt: $prompt")
            
            val response = model.generateContent(prompt)
            val translatedText = response.text?.trim() ?: indonesianText
            
            Log.d(TAG, "Translation successful: '$indonesianText' -> '$translatedText'")
            return@withContext translatedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed for text: '$indonesianText'", e)
            return@withContext indonesianText
        }
    }
    
    /**
     * Translate text to current system language
     * @param indonesianText Text in Indonesian language
     * @param context Context to get current locale
     * @return Translated text or original text if translation fails
     */
    suspend fun translateToSystemLanguage(
        indonesianText: String,
        context: Context
    ): String = withContext(Dispatchers.IO) {
        try {
            val currentLocale = context.resources.configuration.locales[0]
            val languageCode = currentLocale.language
            
            Log.d(TAG, "Current system language: $languageCode")
            
            if (languageCode == "id") {
                Log.d(TAG, "System language is Indonesian, no translation needed")
                return@withContext indonesianText
            }
            
            return@withContext translateToLanguage(indonesianText, languageCode)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system language", e)
            return@withContext indonesianText
        }
    }
    
    /**
     * Build translation prompt for Gemini API
     */
    private fun buildTranslationPrompt(indonesianText: String, targetLanguage: String): String {
        val languageNames = mapOf(
            "en" to "English",
            "ja" to "Japanese",
            "ko" to "Korean",
            "th" to "Thai",
            "id" to "Indonesian"
        )
        
        val targetLanguageName = languageNames[targetLanguage] ?: targetLanguage
        
        return """
            You are a professional translator. Translate the following Indonesian text to $targetLanguageName.
            
            Rules:
            1. Maintain the original meaning and context
            2. Keep medical/technical terms accurate
            3. Use natural, fluent language
            4. Preserve any formatting or structure
            5. Only return the translated text, no explanations
            
            Indonesian text: "$indonesianText"
            
            $targetLanguageName translation:
        """.trimIndent()
    }
    
    /**
     * Check if translation service is available
     */
    fun isAvailable(): Boolean {
        return generativeModel != null
    }
    
    /**
     * Get supported languages
     */
    fun getSupportedLanguages(): List<String> {
        return listOf("id", "en", "ja", "ko", "th")
    }
}
