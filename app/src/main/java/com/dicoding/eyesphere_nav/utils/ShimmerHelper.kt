package com.dicoding.eyesphere_nav.utils

import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import android.util.Log

/**
 * Helper object untuk mengelola shimmer effect di DetailActivity
 */
object ShimmerHelper {
    
    private const val TAG = "ShimmerHelper"
    
    /**
     * Tampilkan shimmer effect untuk title
     */
    fun showTitleShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            textView.visibility = android.view.View.GONE
            shimmerLayout.visibility = android.view.View.VISIBLE
            shimmerLayout.startShimmer()
            Log.d(TAG, "Title shimmer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing title shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk title
     */
    fun hideTitleShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = android.view.View.GONE
            textView.visibility = android.view.View.VISIBLE
            Log.d(TAG, "Title shimmer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding title shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer effect untuk description
     */
    fun showDescriptionShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            textView.visibility = android.view.View.GONE
            shimmerLayout.visibility = android.view.View.VISIBLE
            shimmerLayout.startShimmer()
            Log.d(TAG, "Description shimmer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing description shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk description
     */
    fun hideDescriptionShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = android.view.View.GONE
            textView.visibility = android.view.View.VISIBLE
            Log.d(TAG, "Description shimmer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding description shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer effect untuk date
     */
    fun showDateShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            textView.visibility = android.view.View.GONE
            shimmerLayout.visibility = android.view.View.VISIBLE
            shimmerLayout.startShimmer()
            Log.d(TAG, "Date shimmer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing date shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk date
     */
    fun hideDateShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = android.view.View.GONE
            textView.visibility = android.view.View.VISIBLE
            Log.d(TAG, "Date shimmer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding date shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer effect untuk time
     */
    fun showTimeShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            textView.visibility = android.view.View.GONE
            shimmerLayout.visibility = android.view.View.VISIBLE
            shimmerLayout.startShimmer()
            Log.d(TAG, "Time shimmer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing time shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk time
     */
    fun hideTimeShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = android.view.View.GONE
            textView.visibility = android.view.View.VISIBLE
            Log.d(TAG, "Time shimmer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding time shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer effect untuk recommendation
     */
    fun showRecommendationShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            textView.visibility = android.view.View.GONE
            shimmerLayout.visibility = android.view.View.VISIBLE
            shimmerLayout.startShimmer()
            Log.d(TAG, "Recommendation shimmer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing recommendation shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk recommendation
     */
    fun hideRecommendationShimmer(textView: TextView, shimmerLayout: ShimmerFrameLayout) {
        try {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = android.view.View.GONE
            textView.visibility = android.view.View.VISIBLE
            Log.d(TAG, "Recommendation shimmer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding recommendation shimmer: ${e.message}", e)
        }
    }
}
