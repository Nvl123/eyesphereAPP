package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import java.util.*

object LanguageManager {
    
    private const val PREF_NAME = "language_preferences"
    private const val KEY_LANGUAGE = "selected_language"
    
    // Supported languages
    const val LANGUAGE_INDONESIA = "id"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_JAPANESE = "ja"
    const val LANGUAGE_KOREAN = "ko"
    const val LANGUAGE_THAI = "th"
    
    // Default language
    const val DEFAULT_LANGUAGE = LANGUAGE_INDONESIA
    
    /**
     * Get current language from SharedPreferences
     */
    fun getCurrentLanguage(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Save selected language to SharedPreferences
     */
    fun saveLanguage(context: Context, languageCode: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        Log.d("LanguageManager", "Language saved: $languageCode")
    }
    
    /**
     * Apply language to the application
     */
    fun applyLanguage(context: Context, languageCode: String) {
        try {
            val locale = when (languageCode) {
                LANGUAGE_INDONESIA -> Locale("id", "ID")
                LANGUAGE_ENGLISH -> Locale("en", "US")
                LANGUAGE_JAPANESE -> Locale("ja", "JP")
                LANGUAGE_KOREAN -> Locale("ko", "KR")
                LANGUAGE_THAI -> Locale("th", "TH")
                else -> Locale("id", "ID")
            }
            
            updateResources(context, locale)
            Log.d("LanguageManager", "Language applied: $languageCode")
        } catch (e: Exception) {
            Log.e("LanguageManager", "Error applying language: ${e.message}", e)
        }
    }
    
    /**
     * Update resources with new locale
     */
    private fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    
    /**
     * Get language display name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_INDONESIA -> "Indonesia"
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_JAPANESE -> "日本語"
            LANGUAGE_KOREAN -> "한국어"
            LANGUAGE_THAI -> "ไทย"
            else -> "Indonesia"
        }
    }
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            LANGUAGE_INDONESIA to "Indonesia",
            LANGUAGE_ENGLISH to "English",
            LANGUAGE_JAPANESE to "日本語",
            LANGUAGE_KOREAN to "한국어",
            LANGUAGE_THAI to "ไทย"
        )
    }
    
    /**
     * Check if language is supported
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return languageCode in listOf(
            LANGUAGE_INDONESIA,
            LANGUAGE_ENGLISH,
            LANGUAGE_JAPANESE,
            LANGUAGE_KOREAN,
            LANGUAGE_THAI
        )
    }
    
    /**
     * Get language code from display name
     */
    fun getLanguageCodeFromDisplayName(displayName: String): String {
        return when (displayName) {
            "Indonesia" -> LANGUAGE_INDONESIA
            "English" -> LANGUAGE_ENGLISH
            "日本語" -> LANGUAGE_JAPANESE
            "한국어" -> LANGUAGE_KOREAN
            "ไทย" -> LANGUAGE_THAI
            else -> DEFAULT_LANGUAGE
        }
    }
}
