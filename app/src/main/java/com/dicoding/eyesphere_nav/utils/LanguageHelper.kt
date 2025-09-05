package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dicoding.eyesphere_nav.R

object LanguageHelper {
    
    private const val TAG = "LanguageHelper"
    
    /**
     * Show language selection dialog and handle language change
     */
    fun showLanguageSelectionDialog(
        fragment: Fragment,
        onLanguageChanged: (String) -> Unit
    ) {
        try {
            val dialog = com.dicoding.eyesphere_nav.ui.dialog.LanguageSelectionDialog.newInstance()
            dialog.setOnLanguageSelectedListener(object : com.dicoding.eyesphere_nav.ui.dialog.LanguageSelectionDialog.OnLanguageSelectedListener {
                override fun onLanguageSelected(languageCode: String) {
                    changeLanguage(fragment.requireContext(), languageCode)
                    onLanguageChanged(languageCode)
                }
            })
            dialog.show(fragment.childFragmentManager, "LanguageSelectionDialog")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing language selection dialog: ${e.message}", e)
        }
    }
    
    /**
     * Show language selection dialog and handle language change for Activity
     */
    fun showLanguageSelectionDialog(
        activity: FragmentActivity,
        onLanguageChanged: (String) -> Unit
    ) {
        try {
            val dialog = com.dicoding.eyesphere_nav.ui.dialog.LanguageSelectionDialog.newInstance()
            dialog.setOnLanguageSelectedListener(object : com.dicoding.eyesphere_nav.ui.dialog.LanguageSelectionDialog.OnLanguageSelectedListener {
                override fun onLanguageSelected(languageCode: String) {
                    changeLanguage(activity, languageCode)
                    onLanguageChanged(languageCode)
                }
            })
            dialog.show(activity.supportFragmentManager, "LanguageSelectionDialog")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing language selection dialog: ${e.message}", e)
        }
    }
    
    /**
     * Change language and recreate activity/fragment
     */
    private fun changeLanguage(context: Context, languageCode: String) {
        try {
            if (LanguageManager.isLanguageSupported(languageCode)) {
                LanguageManager.saveLanguage(context, languageCode)
                LanguageManager.applyLanguage(context, languageCode)
                Log.d(TAG, "Language changed to: $languageCode")
                
                // Show success message
                showLanguageChangeMessage(context, true)
            } else {
                Log.w(TAG, "Unsupported language: $languageCode")
                showLanguageChangeMessage(context, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error changing language: ${e.message}", e)
            showLanguageChangeMessage(context, false)
        }
    }
    
    /**
     * Show language change success/failure message
     */
    private fun showLanguageChangeMessage(context: Context, success: Boolean) {
        try {
            val message = if (success) {
                context.getString(R.string.language_changed)
            } else {
                context.getString(R.string.language_change_failed)
            }
            
            // You can customize this to show Toast, Snackbar, or Dialog
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing language change message: ${e.message}", e)
        }
    }
    
    /**
     * Get translated text for server response
     * 
     * ⚠️ DEPRECATED: This method is deprecated and will be removed in future versions.
     * 
     * Use TranslationHelper.translateServerResponseAsync() instead for proper translation:
     * 
     * TranslationHelper.translateServerResponseAsync(context, indonesianText) { translatedText ->
     *     // Use translatedText here
     * }
     */
    @Deprecated("Use TranslationHelper.translateServerResponseAsync() instead")
    fun getTranslatedText(context: Context, indonesianText: String): String {
        Log.w(TAG, "getTranslatedText() is deprecated. Use TranslationHelper.translateServerResponseAsync() instead")
        // Fallback to original text for backward compatibility
        return indonesianText
    }
    
    /**
     * Get translated string resource
     * 
     * ⚠️ DEPRECATED: This method is deprecated and will be removed in future versions.
     * 
     * Use TranslationHelper.translateServerResponseAsync() instead for proper translation:
     * 
     * TranslationHelper.translateServerResponseAsync(context, context.getString(stringResId)) { translatedText ->
     *     // Use translatedText here
     * }
     */
    @Deprecated("Use TranslationHelper.translateServerResponseAsync() instead")
    fun getTranslatedString(context: Context, stringResId: Int): String {
        Log.w(TAG, "getTranslatedString() is deprecated. Use TranslationHelper.translateServerResponseAsync() instead")
        // Fallback to original string resource for backward compatibility
        return try {
            context.getString(stringResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting string resource: ${e.message}", e)
            "String not found"
        }
    }
    
    /**
     * Check if current language is Indonesian
     */
    fun isIndonesian(context: Context): Boolean {
        return LanguageManager.getCurrentLanguage(context) == LanguageManager.LANGUAGE_INDONESIA
    }
    
    /**
     * Check if current language is English
     */
    fun isEnglish(context: Context): Boolean {
        return LanguageManager.getCurrentLanguage(context) == LanguageManager.LANGUAGE_ENGLISH
    }
    
    /**
     * Check if current language is Japanese
     */
    fun isJapanese(context: Context): Boolean {
        return LanguageManager.getCurrentLanguage(context) == LanguageManager.LANGUAGE_JAPANESE
    }
    
    /**
     * Check if current language is Korean
     */
    fun isKorean(context: Context): Boolean {
        return LanguageManager.getCurrentLanguage(context) == LanguageManager.LANGUAGE_KOREAN
    }
    
    /**
     * Check if current language is Thai
     */
    fun isThai(context: Context): Boolean {
        return LanguageManager.getCurrentLanguage(context) == LanguageManager.LANGUAGE_THAI
    }
    
    /**
     * Check if translation service is available
     * Use this to check if TranslationHelper can be used
     */
    fun isTranslationAvailable(context: Context): Boolean {
        return try {
            com.dicoding.eyesphere_nav.utils.TranslationHelper.isTranslationAvailable()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking translation availability", e)
            false
        }
    }
    
    /**
     * Get current system language code
     * Use this to check current system language
     */
    fun getCurrentSystemLanguage(context: Context): String {
        return try {
            com.dicoding.eyesphere_nav.utils.TranslationHelper.getCurrentSystemLanguage(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system language", e)
            "id" // Default to Indonesian
        }
    }
    
    /**
     * Check if current system language is Indonesian
     * Use this to determine if translation is needed
     */
    fun isSystemLanguageIndonesian(context: Context): Boolean {
        return try {
            com.dicoding.eyesphere_nav.utils.TranslationHelper.isSystemLanguageIndonesian(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if system language is Indonesian", e)
            true // Default to Indonesian (no translation needed)
        }
    }
}
