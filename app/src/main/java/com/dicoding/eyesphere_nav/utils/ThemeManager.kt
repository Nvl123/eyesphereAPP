package com.dicoding.eyesphere_nav.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.dicoding.eyesphere_nav.R

object ThemeManager {
    
    private const val PREF_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // Theme modes
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    /**
     * Get current theme mode from SharedPreferences
     */
    fun getCurrentThemeMode(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    /**
     * Save theme mode to SharedPreferences
     */
    fun saveThemeMode(context: Context, themeMode: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_THEME_MODE, themeMode).apply()
    }
    
    /**
     * Apply theme mode to the application
     */
    fun applyTheme(themeMode: String) {
        android.util.Log.d("ThemeManager", "Applying theme: $themeMode")
        when (themeMode) {
            THEME_LIGHT -> {
                android.util.Log.d("ThemeManager", "Setting light theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                android.util.Log.d("ThemeManager", "Setting dark theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_SYSTEM -> {
                android.util.Log.d("ThemeManager", "Setting system theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    
    /**
     * Check if current theme is dark mode
     */
    fun isDarkMode(context: Context): Boolean {
        val currentThemeMode = getCurrentThemeMode(context)
        return when (currentThemeMode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> {
                val uiMode = context.resources.configuration.uiMode
                (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
    
    /**
     * Toggle between light and dark themes
     */
    fun toggleTheme(context: Context) {
        val currentMode = getCurrentThemeMode(context)
        val newMode = if (currentMode == THEME_DARK) THEME_LIGHT else THEME_DARK
        saveThemeMode(context, newMode)
        applyTheme(newMode)
    }
    
    /**
     * Get appropriate text color based on current theme
     */
    fun getTextColor(context: Context): Int {
        return if (isDarkMode(context)) {
            ContextCompat.getColor(context, R.color.text_primary_dark)
        } else {
            ContextCompat.getColor(context, R.color.text_primary_light)
        }
    }
    
    /**
     * Get appropriate background color based on current theme
     */
    fun getBackgroundColor(context: Context): Int {
        return if (isDarkMode(context)) {
            ContextCompat.getColor(context, R.color.background_dark)
        } else {
            ContextCompat.getColor(context, R.color.background_light)
        }
    }
    
    /**
     * Get appropriate card background color based on current theme
     */
    fun getCardBackgroundColor(context: Context): Int {
        return if (isDarkMode(context)) {
            ContextCompat.getColor(context, R.color.card_background_dark)
        } else {
            ContextCompat.getColor(context, R.color.card_background_light)
        }
    }
}

