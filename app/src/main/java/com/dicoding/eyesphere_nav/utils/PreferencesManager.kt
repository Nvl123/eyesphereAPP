package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager private constructor(context: Context) {
    
    companion object {
        private const val PREF_NAME = "eyesphere_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_LANGUAGE = "language"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    var themeMode: String
        get() = sharedPreferences.getString(KEY_THEME_MODE, ThemeManager.THEME_SYSTEM) ?: ThemeManager.THEME_SYSTEM
        set(value) = sharedPreferences.edit().putString(KEY_THEME_MODE, value).apply()
    
    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()
    
    var language: String
        get() = sharedPreferences.getString(KEY_LANGUAGE, "id") ?: "id"
        set(value) = sharedPreferences.edit().putString(KEY_LANGUAGE, value).apply()
    
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
