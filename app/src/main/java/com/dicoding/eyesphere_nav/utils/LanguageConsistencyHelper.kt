package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class to ensure language consistency across the app
 * This prevents conflicts between user preference and system language
 * OPTIMIZED for performance - minimal UI thread load
 */
object LanguageConsistencyHelper {
    
    private const val TAG = "LanguageConsistencyHelper"
    
    // Cache untuk menghindari operasi berulang
    private var cachedUserLanguage: String? = null
    private var cachedSystemLanguage: String? = null
    private var cachedShouldTranslateUI: Boolean? = null
    private var cachedShouldTranslateServer: Boolean? = null
    private var lastCacheTime: Long = 0
    private const val CACHE_DURATION = 5000L // 5 detik
    
    /**
     * Check if translation should be performed for UI elements
     * Only translate if user explicitly selected a non-Indonesian language
     * OPTIMIZED: Uses caching to reduce UI thread load
     */
    fun shouldTranslateUI(context: Context): Boolean {
        return try {
            // Check cache first for instant response
            if (isCacheValid()) {
                return cachedShouldTranslateUI ?: false
            }
            
            // Perform operation in background if needed
            CoroutineScope(Dispatchers.Default).launch {
                updateCache(context)
            }
            
            // Return cached value or default
            cachedShouldTranslateUI ?: false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if should translate UI", e)
            false // Default to no translation on error
        }
    }
    
    /**
     * Check if translation should be performed for server responses
     * Always translate server responses if system language is not Indonesian
     * OPTIMIZED: Uses caching to reduce UI thread load
     */
    fun shouldTranslateServerResponse(context: Context): Boolean {
        return try {
            // Check cache first for instant response
            if (isCacheValid()) {
                return cachedShouldTranslateServer ?: false
            }
            
            // Perform operation in background if needed
            CoroutineScope(Dispatchers.Default).launch {
                updateCache(context)
            }
            
            // Return cached value or default
            cachedShouldTranslateServer ?: false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if should translate server response", e)
            false // Default to no translation on error
        }
    }
    
    /**
     * Get current effective language for UI display
     * Returns user preference if set, otherwise system language
     * OPTIMIZED: Uses cached values
     */
    fun getEffectiveLanguage(context: Context): String {
        return try {
            // Use cached values if available
            val userLanguage = cachedUserLanguage ?: LanguageManager.getCurrentLanguage(context)
            val systemLanguage = cachedSystemLanguage ?: TranslationHelper.getCurrentSystemLanguage(context)
            
            // If user has explicitly set a language, use that
            // Otherwise, use system language
            val effectiveLanguage = if (userLanguage != LanguageManager.DEFAULT_LANGUAGE) {
                userLanguage
            } else {
                systemLanguage
            }
            
            // Log only in debug builds and in background
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d(TAG, "Effective language: $effectiveLanguage (User: $userLanguage, System: $systemLanguage)")
                }
            }
            
            effectiveLanguage
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting effective language", e)
            LanguageManager.DEFAULT_LANGUAGE // Default to Indonesian on error
        }
    }
    
    /**
     * Check if current language is Indonesian (either user preference or system)
     * OPTIMIZED: Uses cached values
     */
    fun isCurrentLanguageIndonesian(context: Context): Boolean {
        return try {
            val effectiveLanguage = getEffectiveLanguage(context)
            val isIndonesian = effectiveLanguage == LanguageManager.LANGUAGE_INDONESIA
            
            // Log only in debug builds and in background
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d(TAG, "Is current language Indonesian: $isIndonesian ($effectiveLanguage)")
                }
            }
            
            isIndonesian
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if current language is Indonesian", e)
            true // Default to Indonesian on error
        }
    }
    
    /**
     * Get language display name for current effective language
     * OPTIMIZED: Uses cached values
     */
    fun getCurrentLanguageDisplayName(context: Context): String {
        return try {
            val effectiveLanguage = getEffectiveLanguage(context)
            val displayName = LanguageManager.getLanguageDisplayName(effectiveLanguage)
            
            // Log only in debug builds and in background
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d(TAG, "Current language display name: $displayName ($effectiveLanguage)")
                }
            }
            
            displayName
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current language display name", e)
            "Indonesia" // Default to Indonesia on error
        }
    }
    
    /**
     * Log current language status for debugging
     * OPTIMIZED: Runs in background thread
     */
    fun logLanguageStatus(context: Context) {
        // Only log in debug builds
        if (!Log.isLoggable(TAG, Log.DEBUG)) {
            return
        }
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val userLanguage = cachedUserLanguage ?: LanguageManager.getCurrentLanguage(context)
                val systemLanguage = cachedSystemLanguage ?: TranslationHelper.getCurrentSystemLanguage(context)
                val effectiveLanguage = getEffectiveLanguage(context)
                val shouldTranslateUI = cachedShouldTranslateUI ?: false
                val shouldTranslateServer = cachedShouldTranslateServer ?: false
                
                Log.d(TAG, "=== Language Status ===")
                Log.d(TAG, "User preference: $userLanguage")
                Log.d(TAG, "System language: $systemLanguage")
                Log.d(TAG, "Effective language: $effectiveLanguage")
                Log.d(TAG, "Should translate UI: $shouldTranslateUI")
                Log.d(TAG, "Should translate server: $shouldTranslateServer")
                Log.d(TAG, "Translation available: ${TranslationHelper.isTranslationAvailable()}")
                Log.d(TAG, "=======================")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error logging language status", e)
            }
        }
    }
    
    /**
     * Force refresh cache (useful when language changes)
     */
    fun refreshCache(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            updateCache(context)
        }
    }
    
    /**
     * Clear cache (useful for testing or memory management)
     */
    fun clearCache() {
        cachedUserLanguage = null
        cachedSystemLanguage = null
        cachedShouldTranslateUI = null
        cachedShouldTranslateServer = null
        lastCacheTime = 0
        Log.d(TAG, "Language cache cleared")
    }
    
    /**
     * Check if cache is still valid
     */
    private fun isCacheValid(): Boolean {
        return cachedUserLanguage != null && 
               cachedSystemLanguage != null && 
               cachedShouldTranslateUI != null && 
               cachedShouldTranslateServer != null &&
               (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION
    }
    
    /**
     * Update cache with fresh values
     * Runs in background thread to avoid UI blocking
     */
    private suspend fun updateCache(context: Context) {
        try {
            val userLanguage = LanguageManager.getCurrentLanguage(context)
            val systemLanguage = TranslationHelper.getCurrentSystemLanguage(context)
            
            // Calculate translation flags
            val shouldTranslateUI = userLanguage != LanguageManager.LANGUAGE_INDONESIA && 
                                  TranslationHelper.isTranslationAvailable() &&
                                  systemLanguage != "id"
            
            // Fix: Server response should only be translated if EFFECTIVE language is not Indonesian
            // This prevents translating when user explicitly chooses Indonesian
            val effectiveLanguage = if (userLanguage != LanguageManager.DEFAULT_LANGUAGE) {
                userLanguage
            } else {
                systemLanguage
            }
            
            val shouldTranslateServer = TranslationHelper.isTranslationAvailable() &&
                                      effectiveLanguage != "id" &&
                                      effectiveLanguage != LanguageManager.LANGUAGE_INDONESIA
            
            // Update cache
            cachedUserLanguage = userLanguage
            cachedSystemLanguage = systemLanguage
            cachedShouldTranslateUI = shouldTranslateUI
            cachedShouldTranslateServer = shouldTranslateServer
            lastCacheTime = System.currentTimeMillis()
            
            // Log cache update in background
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Cache updated - User: $userLanguage, System: $systemLanguage")
                Log.d(TAG, "Effective language: $effectiveLanguage")
                Log.d(TAG, "Should translate UI: $shouldTranslateUI, Server: $shouldTranslateServer")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating language cache", e)
            // Clear cache on error to force refresh
            clearCache()
        }
    }
}
