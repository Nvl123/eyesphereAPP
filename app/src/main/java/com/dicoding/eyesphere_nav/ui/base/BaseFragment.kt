package com.dicoding.eyesphere_nav.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dicoding.eyesphere_nav.utils.ThemeManager

abstract class BaseFragment : Fragment() {
    
    protected var isDarkMode: Boolean = false
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ensure ActionBar is hidden when fragment is created
        hideActionBar()
        
        // Check current theme mode
        updateThemeMode()
        
        // Apply theme-specific styling
        applyThemeStyling()
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure ActionBar is hidden when fragment resumes
        hideActionBar()
        
        // Update theme when returning to this fragment
        val newDarkMode = ThemeManager.isDarkMode(requireContext())
        if (newDarkMode != isDarkMode) {
            android.util.Log.d("BaseFragment", "Theme changed from $isDarkMode to $newDarkMode")
            isDarkMode = newDarkMode
            applyThemeStyling()
        }
        
        // Ensure theme is properly applied
        ensureThemeApplied()
    }
    
    /**
     * Helper method to ensure ActionBar is hidden
     */
    protected fun hideActionBar() {
        try {
            (activity as? AppCompatActivity)?.supportActionBar?.hide()
        } catch (e: Exception) {
            // Ignore exceptions when hiding ActionBar
        }
    }
    
    /**
     * Ensure theme is properly applied
     */
    protected fun ensureThemeApplied() {
        try {
            val currentThemeMode = ThemeManager.getCurrentThemeMode(requireContext())
            val currentNightMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode()
            
            android.util.Log.d("BaseFragment", "Current theme mode: $currentThemeMode, Current night mode: $currentNightMode")
            
            // Check if theme needs to be reapplied
            val shouldBeDark = when (currentThemeMode) {
                ThemeManager.THEME_DARK -> true
                ThemeManager.THEME_LIGHT -> false
                ThemeManager.THEME_SYSTEM -> {
                    val uiMode = resources.configuration.uiMode
                    (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                }
                else -> false
            }
            
            val isCurrentlyDark = currentNightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES || 
                                (currentNightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM && 
                                 (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            
            android.util.Log.d("BaseFragment", "Should be dark: $shouldBeDark, Is currently dark: $isCurrentlyDark")
            
            if (shouldBeDark != isCurrentlyDark) {
                android.util.Log.d("BaseFragment", "Theme mismatch detected, reapplying theme")
                ThemeManager.applyTheme(currentThemeMode)
            }
        } catch (e: Exception) {
            android.util.Log.e("BaseFragment", "Error ensuring theme applied", e)
        }
    }
    
    private fun updateThemeMode() {
        try {
            isDarkMode = ThemeManager.isDarkMode(requireContext())
        } catch (e: Exception) {
            isDarkMode = false
        }
    }
    
    /**
     * Override this method in child fragments to apply theme-specific styling
     */
    protected open fun applyThemeStyling() {
        // Default implementation - override in child classes
    }
    
    /**
     * Get appropriate text color based on current theme
     */
    protected fun getThemeTextColor(): Int {
        return if (isDarkMode) {
            ThemeManager.getTextColor(requireContext())
        } else {
            ThemeManager.getTextColor(requireContext())
        }
    }
    
    /**
     * Get appropriate background color based on current theme
     */
    protected fun getThemeBackgroundColor(): Int {
        return if (isDarkMode) {
            ThemeManager.getBackgroundColor(requireContext())
        } else {
            ThemeManager.getBackgroundColor(requireContext())
        }
    }
    
    /**
     * Get appropriate card background color based on current theme
     */
    protected fun getThemeCardBackgroundColor(): Int {
        return if (isDarkMode) {
            ThemeManager.getCardBackgroundColor(requireContext())
        } else {
            ThemeManager.getCardBackgroundColor(requireContext())
        }
    }
}
