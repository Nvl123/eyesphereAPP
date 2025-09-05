package com.dicoding.eyesphere_nav

import android.app.Application
import com.dicoding.eyesphere_nav.utils.ThemeManager
import com.dicoding.eyesphere_nav.utils.LanguageManager
import com.dicoding.eyesphere_nav.utils.NotificationHelper

class EyesphereApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme from saved preferences
        initializeTheme()
        
        // Initialize language from saved preferences
        initializeLanguage()
        
        // Initialize notification channels
        initializeNotificationChannels()
    }
    
    private fun initializeTheme() {
        try {
            val savedThemeMode = ThemeManager.getCurrentThemeMode(this)
            ThemeManager.applyTheme(savedThemeMode)
        } catch (e: Exception) {
            // Fallback to system theme if there's an error
            ThemeManager.applyTheme(ThemeManager.THEME_SYSTEM)
        }
    }
    
    private fun initializeLanguage() {
        try {
            val savedLanguage = LanguageManager.getCurrentLanguage(this)
            LanguageManager.applyLanguage(this, savedLanguage)
        } catch (e: Exception) {
            // Fallback to Indonesian language if there's an error
            LanguageManager.applyLanguage(this, LanguageManager.LANGUAGE_INDONESIA)
        }
    }
    
    private fun initializeNotificationChannels() {
        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            // Log error but don't crash app
            android.util.Log.e("EyesphereApplication", "Error creating notification channels", e)
        }
    }
}
