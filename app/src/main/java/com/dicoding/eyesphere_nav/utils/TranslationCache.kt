package com.dicoding.eyesphere_nav.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Cache utility for storing and retrieving translated results
 * This prevents unnecessary re-translation of the same text
 */
object TranslationCache {
    
    private const val TAG = "TranslationCache"
    private const val PREF_NAME = "translation_cache"
    private const val KEY_CACHE_SIZE = "cache_size"
    private const val MAX_CACHE_SIZE = 100 // Maximum number of cached items
    
    private val gson = Gson()
    
    /**
     * Cache item containing original and translated text
     */
    data class CacheItem(
        val originalText: String,
        val translatedText: String,
        val cleanedText: String,
        val language: String,
        val timestamp: Long
    )
    
    /**
     * Get cached translation for text
     */
    fun getCachedTranslation(context: Context, originalText: String, targetLanguage: String): CacheItem? {
        return try {
            val prefs = getSharedPreferences(context)
            val cacheKey = generateCacheKey(originalText, targetLanguage)
            val cachedJson = prefs.getString(cacheKey, null)
            
            if (cachedJson != null) {
                val cachedItem = gson.fromJson(cachedJson, CacheItem::class.java)
                
                // Check if cache is still valid (less than 24 hours old)
                val isExpired = System.currentTimeMillis() - cachedItem.timestamp > 24 * 60 * 60 * 1000
                
                if (!isExpired) {
                    Log.d(TAG, "Cache hit for: '$originalText' -> '${cachedItem.translatedText}'")
                    return cachedItem
                } else {
                    // Remove expired cache
                    removeCachedTranslation(context, originalText, targetLanguage)
                    Log.d(TAG, "Cache expired for: '$originalText'")
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached translation", e)
            null
        }
    }
    
    /**
     * Cache translation result
     */
    fun cacheTranslation(
        context: Context, 
        originalText: String, 
        translatedText: String, 
        cleanedText: String, 
        targetLanguage: String
    ) {
        try {
            val prefs = getSharedPreferences(context)
            val cacheKey = generateCacheKey(originalText, targetLanguage)
            
            val cacheItem = CacheItem(
                originalText = originalText,
                translatedText = translatedText,
                cleanedText = cleanedText,
                language = targetLanguage,
                timestamp = System.currentTimeMillis()
            )
            
            val cacheJson = gson.toJson(cacheItem)
            prefs.edit().putString(cacheKey, cacheJson).apply()
            
            // Update cache size
            updateCacheSize(prefs, 1)
            
            Log.d(TAG, "Cached translation: '$originalText' -> '$translatedText'")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error caching translation", e)
        }
    }
    
    /**
     * Remove cached translation
     */
    fun removeCachedTranslation(context: Context, originalText: String, targetLanguage: String) {
        try {
            val prefs = getSharedPreferences(context)
            val cacheKey = generateCacheKey(originalText, targetLanguage)
            prefs.edit().remove(cacheKey).apply()
            
            // Update cache size
            updateCacheSize(prefs, -1)
            
            Log.d(TAG, "Removed cached translation for: '$originalText'")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing cached translation", e)
        }
    }
    
    /**
     * Clear all cached translations
     */
    fun clearCache(context: Context) {
        try {
            val prefs = getSharedPreferences(context)
            prefs.edit().clear().apply()
            
            Log.d(TAG, "Translation cache cleared")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing translation cache", e)
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(context: Context): CacheStats {
        return try {
            val prefs = getSharedPreferences(context)
            val cacheSize = prefs.getInt(KEY_CACHE_SIZE, 0)
            
            CacheStats(
                totalItems = cacheSize,
                maxItems = MAX_CACHE_SIZE,
                usagePercentage = (cacheSize.toFloat() / MAX_CACHE_SIZE * 100).toInt()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache stats", e)
            CacheStats(0, MAX_CACHE_SIZE, 0)
        }
    }
    
    /**
     * Check if text should be cached
     */
    fun shouldCache(text: String): Boolean {
        // Only cache meaningful text (not empty, not too short, not too long)
        return text.isNotEmpty() && text.length > 3 && text.length < 1000
    }
    
    /**
     * Generate cache key for text and language combination
     */
    private fun generateCacheKey(originalText: String, targetLanguage: String): String {
        // Create a hash-based key for efficient storage
        val hash = (originalText + targetLanguage).hashCode().toString()
        return "trans_$hash"
    }
    
    /**
     * Update cache size counter
     */
    private fun updateCacheSize(prefs: SharedPreferences, delta: Int) {
        val currentSize = prefs.getInt(KEY_CACHE_SIZE, 0)
        val newSize = (currentSize + delta).coerceAtLeast(0).coerceAtMost(MAX_CACHE_SIZE)
        prefs.edit().putInt(KEY_CACHE_SIZE, newSize).apply()
    }
    
    /**
     * Get SharedPreferences instance
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Cache statistics
     */
    data class CacheStats(
        val totalItems: Int,
        val maxItems: Int,
        val usagePercentage: Int
    )
}
