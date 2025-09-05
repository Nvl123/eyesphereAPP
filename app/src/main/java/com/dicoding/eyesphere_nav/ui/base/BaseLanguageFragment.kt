package com.dicoding.eyesphere_nav.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.dicoding.eyesphere_nav.utils.LanguageManager
import java.util.*

abstract class BaseLanguageFragment : Fragment() {
    
    private var currentLanguage: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = LanguageManager.getCurrentLanguage(requireContext())
        Log.d("BaseLanguageFragment", "Fragment created with language: $currentLanguage")
    }
    
    override fun onResume() {
        super.onResume()
        val newLanguage = LanguageManager.getCurrentLanguage(requireContext())
        if (newLanguage != currentLanguage) {
            Log.d("BaseLanguageFragment", "Language changed from $currentLanguage to $newLanguage")
            currentLanguage = newLanguage
            onLanguageChanged()
        }
    }
    
    /**
     * Override this method in child fragments to handle language changes
     */
    protected open fun onLanguageChanged() {
        // Default implementation - override in child classes
        Log.d("BaseLanguageFragment", "Language changed to: $currentLanguage")
    }
    
    /**
     * Get current language code
     */
    protected fun getCurrentLanguage(): String {
        return currentLanguage
    }
    
    /**
     * Check if current language matches the given language code
     */
    protected fun isCurrentLanguage(languageCode: String): Boolean {
        return currentLanguage == languageCode
    }
    
    /**
     * Apply language to the fragment context
     */
    protected fun applyLanguageToContext(context: Context): Context {
        val language = LanguageManager.getCurrentLanguage(context)
        val locale = when (language) {
            LanguageManager.LANGUAGE_INDONESIA -> Locale("id", "ID")
            LanguageManager.LANGUAGE_ENGLISH -> Locale("en", "US")
            LanguageManager.LANGUAGE_JAPANESE -> Locale("ja", "JP")
            LanguageManager.LANGUAGE_KOREAN -> Locale("ko", "KR")
            LanguageManager.LANGUAGE_THAI -> Locale("th", "TH")
            else -> Locale("id", "ID")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
