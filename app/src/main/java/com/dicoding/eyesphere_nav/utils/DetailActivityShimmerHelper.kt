package com.dicoding.eyesphere_nav.utils

import android.view.View
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import android.util.Log

/**
 * Helper class untuk mengelola shimmer effect di DetailActivity
 * Memberikan feedback visual selama proses translasi bahasa
 */
object DetailActivityShimmerHelper {
    
    private const val TAG = "DetailShimmer"
    
    /**
     * Tampilkan shimmer untuk title
     */
    fun showTitleShimmer(tvTitle: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Showing title shimmer")
            tvTitle.visibility = View.GONE
            shimmerContainer.visibility = View.VISIBLE
            shimmerContainer.startShimmer()
            Log.d(TAG, "Title shimmer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing title shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk title
     */
    fun hideTitleShimmer(tvTitle: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Hiding title shimmer")
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            Log.d(TAG, "Title shimmer hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding title shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer untuk description
     */
    fun showDescriptionShimmer(tvDescription: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Showing description shimmer")
            tvDescription.visibility = View.GONE
            shimmerContainer.visibility = View.VISIBLE
            shimmerContainer.startShimmer()
            Log.d(TAG, "Description shimmer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing description shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk description
     */
    fun hideDescriptionShimmer(tvDescription: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Hiding description shimmer")
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE
            tvDescription.visibility = View.VISIBLE
            Log.d(TAG, "Description shimmer hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding description shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer untuk date
     */
    fun showDateShimmer(tvDate: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Showing date shimmer")
            tvDate.visibility = View.GONE
            shimmerContainer.visibility = View.VISIBLE
            shimmerContainer.startShimmer()
            Log.d(TAG, "Date shimmer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing date shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk date
     */
    fun hideDateShimmer(tvDate: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Hiding date shimmer")
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE
            tvDate.visibility = View.VISIBLE
            Log.d(TAG, "Date shimmer hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding date shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer untuk time
     */
    fun showTimeShimmer(tvTime: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Showing time shimmer")
            tvTime.visibility = View.GONE
            shimmerContainer.visibility = View.VISIBLE
            shimmerContainer.startShimmer()
            Log.d(TAG, "Time shimmer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing time shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk time
     */
    fun hideTimeShimmer(tvTime: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Hiding time shimmer")
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE
            tvTime.visibility = View.VISIBLE
            Log.d(TAG, "Time shimmer hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding time shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Tampilkan shimmer untuk recommendation
     */
    fun showRecommendationShimmer(tvRecommendation: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Showing recommendation shimmer")
            tvRecommendation.visibility = View.GONE
            shimmerContainer.visibility = View.VISIBLE
            shimmerContainer.startShimmer()
            Log.d(TAG, "Recommendation shimmer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing recommendation shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Sembunyikan shimmer untuk recommendation
     */
    fun hideRecommendationShimmer(tvRecommendation: TextView, shimmerContainer: ShimmerFrameLayout) {
        try {
            Log.d(TAG, "Hiding recommendation shimmer")
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE
            tvRecommendation.visibility = View.VISIBLE
            Log.d(TAG, "Recommendation shimmer hidden successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding recommendation shimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update title dengan shimmer effect
     */
    fun updateTitleWithShimmer(
        tvTitle: TextView, 
        shimmerContainer: ShimmerFrameLayout, 
        newText: String, 
        delayMs: Long = 1500
    ) {
        try {
            Log.d(TAG, "Starting title shimmer update with text: $newText")
            showTitleShimmer(tvTitle, shimmerContainer)
            
            shimmerContainer.postDelayed({
                try {
                    tvTitle.text = newText
                    hideTitleShimmer(tvTitle, shimmerContainer)
                    Log.d(TAG, "Title shimmer update completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in title shimmer update callback: ${e.message}", e)
                }
            }, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTitleWithShimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update description dengan shimmer effect
     */
    fun updateDescriptionWithShimmer(
        tvDescription: TextView, 
        shimmerContainer: ShimmerFrameLayout, 
        newText: String, 
        delayMs: Long = 1500
    ) {
        try {
            Log.d(TAG, "Starting description shimmer update with text: $newText")
            showDescriptionShimmer(tvDescription, shimmerContainer)
            
            shimmerContainer.postDelayed({
                try {
                    tvDescription.text = newText
                    hideDescriptionShimmer(tvDescription, shimmerContainer)
                    Log.d(TAG, "Description shimmer update completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in description shimmer update callback: ${e.message}", e)
                }
            }, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateDescriptionWithShimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update date dengan shimmer effect
     */
    fun updateDateWithShimmer(
        tvDate: TextView, 
        shimmerContainer: ShimmerFrameLayout, 
        newText: String, 
        delayMs: Long = 1000
    ) {
        try {
            Log.d(TAG, "Starting date shimmer update with text: $newText")
            showDateShimmer(tvDate, shimmerContainer)
            
            shimmerContainer.postDelayed({
                try {
                    tvDate.text = newText
                    hideDateShimmer(tvDate, shimmerContainer)
                    Log.d(TAG, "Date shimmer update completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in date shimmer update callback: ${e.message}", e)
                }
            }, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateDateWithShimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update time dengan shimmer effect
     */
    fun updateTimeWithShimmer(
        tvTime: TextView, 
        shimmerContainer: ShimmerFrameLayout, 
        newText: String, 
        delayMs: Long = 1000
    ) {
        try {
            Log.d(TAG, "Starting time shimmer update with text: $newText")
            showTimeShimmer(tvTime, shimmerContainer)
            
            shimmerContainer.postDelayed({
                try {
                    tvTime.text = newText
                    hideTimeShimmer(tvTime, shimmerContainer)
                    Log.d(TAG, "Time shimmer update completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in time shimmer update callback: ${e.message}", e)
                }
            }, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTimeWithShimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update recommendation dengan shimmer effect
     */
    fun updateRecommendationWithShimmer(
        tvRecommendation: TextView, 
        shimmerContainer: ShimmerFrameLayout, 
        newText: String, 
        delayMs: Long = 2000
    ) {
        try {
            Log.d(TAG, "Starting recommendation shimmer update with text: $newText")
            showRecommendationShimmer(tvRecommendation, shimmerContainer)
            
            shimmerContainer.postDelayed({
                try {
                    tvRecommendation.text = newText
                    hideRecommendationShimmer(tvRecommendation, shimmerContainer)
                    Log.d(TAG, "Recommendation shimmer update completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in recommendation shimmer update callback: ${e.message}", e)
                }
            }, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateRecommendationWithShimmer: ${e.message}", e)
        }
    }
    
    /**
     * Update semua elemen dengan shimmer effect secara berurutan
     */
    fun updateAllWithShimmer(
        tvTitle: TextView,
        shimmerTitle: ShimmerFrameLayout,
        tvDescription: TextView,
        shimmerDescription: ShimmerFrameLayout,
        tvDate: TextView,
        shimmerDate: ShimmerFrameLayout,
        tvTime: TextView,
        shimmerTime: ShimmerFrameLayout,
        tvRecommendation: TextView? = null,
        shimmerRecommendation: ShimmerFrameLayout? = null,
        titleText: String,
        descriptionText: String,
        dateText: String,
        timeText: String,
        recommendationText: String? = null
    ) {
        try {
            Log.d(TAG, "Starting sequential shimmer update for all elements")
            
            // Update title first
            updateTitleWithShimmer(tvTitle, shimmerTitle, titleText, 1000)
            
            // Update description after title
            shimmerTitle.postDelayed({
                updateDescriptionWithShimmer(tvDescription, shimmerDescription, descriptionText, 1500)
            }, 1500)
            
            // Update date after description
            shimmerTitle.postDelayed({
                updateDateWithShimmer(tvDate, shimmerDate, dateText, 800)
            }, 3000)
            
            // Update time after date
            shimmerTitle.postDelayed({
                updateTimeWithShimmer(tvTime, shimmerTime, timeText, 800)
            }, 4000)
            
            // Update recommendation if available
            if (tvRecommendation != null && shimmerRecommendation != null && recommendationText != null) {
                shimmerTitle.postDelayed({
                    updateRecommendationWithShimmer(tvRecommendation, shimmerRecommendation, recommendationText, 2000)
                }, 5000)
            }
            
            Log.d(TAG, "Sequential shimmer update scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateAllWithShimmer: ${e.message}", e)
        }
    }
}
