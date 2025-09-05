package com.dicoding.eyesphere_nav.ui.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.eyesphere_nav.utils.LanguageManager
import java.util.*

abstract class BaseLanguageActivity : AppCompatActivity() {
    
    private var currentLanguage: String = ""
    
    override fun attachBaseContext(newBase: Context) {
        val language = LanguageManager.getCurrentLanguage(newBase)
        val locale = when (language) {
            LanguageManager.LANGUAGE_INDONESIA -> Locale("id", "ID")
            LanguageManager.LANGUAGE_ENGLISH -> Locale("en", "US")
            LanguageManager.LANGUAGE_JAPANESE -> Locale("ja", "JP")
            LanguageManager.LANGUAGE_KOREAN -> Locale("ko", "KR")
            LanguageManager.LANGUAGE_THAI -> Locale("th", "TH")
            else -> Locale("id", "ID")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLanguage = LanguageManager.getCurrentLanguage(this)
        Log.d("BaseLanguageActivity", "Activity created with language: $currentLanguage")
    }
    
    override fun onResume() {
        super.onResume()
        val newLanguage = LanguageManager.getCurrentLanguage(this)
        if (newLanguage != currentLanguage) {
            Log.d("BaseLanguageActivity", "Language changed from $currentLanguage to $newLanguage")
            currentLanguage = newLanguage
            recreateActivity()
        }
    }
    
    /**
     * Recreate activity to apply new language
     */
    protected fun recreateActivity() {
        try {
            val intent = Intent(this, this::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("BaseLanguageActivity", "Error recreating activity: ${e.message}", e)
        }
    }
    
    /**
     * Change language and recreate activity
     */
    protected fun changeLanguage(languageCode: String) {
        try {
            if (LanguageManager.isLanguageSupported(languageCode)) {
                LanguageManager.saveLanguage(this, languageCode)
                LanguageManager.applyLanguage(this, languageCode)
                currentLanguage = languageCode
                recreateActivity()
            } else {
                Log.w("BaseLanguageActivity", "Unsupported language: $languageCode")
            }
        } catch (e: Exception) {
            Log.e("BaseLanguageActivity", "Error changing language: ${e.message}", e)
        }
    }
    
    /**
     * Get current language code
     */
    protected fun getCurrentLanguage(): String {
        return currentLanguage
    }
}
