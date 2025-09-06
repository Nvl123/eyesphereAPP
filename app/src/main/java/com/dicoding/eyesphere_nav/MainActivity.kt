package com.dicoding.eyesphere_nav

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import me.ibrahimsn.lib.SmoothBottomBar
import com.dicoding.eyesphere_nav.databinding.ActivityMainBinding
import com.dicoding.eyesphere_nav.utils.ThemeManager
import com.dicoding.eyesphere_nav.utils.TranslationHelper
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import android.app.Activity
import android.util.Log

class MainActivity : AppCompatActivity() {

    // just ceck the branch

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomBar: SmoothBottomBar
    


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme before setting content view
        applySavedTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide ActionBar permanently
        hideActionBar()

        // Menyiapkan SmoothBottomBar
        bottomBar = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Removed setupActionBarWithNavController to prevent ActionBar from appearing
        
        // Handle notification intent
        handleNotificationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Ensure ActionBar stays hidden when activity resumes
        hideActionBar()
        
        // Ensure theme is properly applied when activity resumes
        ensureThemeApplied()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Ensure ActionBar stays hidden when configuration changes (e.g., theme changes)
        hideActionBar()
        
        // Ensure theme is properly applied when configuration changes
        ensureThemeApplied()
    }

    private fun applySavedTheme() {
        try {
            val savedThemeMode = ThemeManager.getCurrentThemeMode(this)
            android.util.Log.d("MainActivity", "Saved theme mode: $savedThemeMode")
            ThemeManager.applyTheme(savedThemeMode)
            android.util.Log.d("MainActivity", "Theme applied successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error applying theme", e)
            // Fallback to system theme if there's an error
            ThemeManager.applyTheme(ThemeManager.THEME_SYSTEM)
        }
    }

    /**
     * Helper method to ensure ActionBar is hidden
     */
    private fun hideActionBar() {
        try {
            supportActionBar?.hide()
        } catch (e: Exception) {
            // Ignore exceptions when hiding ActionBar
        }
    }

    /**
     * Ensure theme is properly applied
     */
    private fun ensureThemeApplied() {
        try {
            val currentThemeMode = ThemeManager.getCurrentThemeMode(this)
            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
            
            android.util.Log.d("MainActivity", "Current theme mode: $currentThemeMode, Current night mode: $currentNightMode")
            
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
            
            val isCurrentlyDark = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES || 
                                (currentNightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM && 
                                 (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            
            android.util.Log.d("MainActivity", "Should be dark: $shouldBeDark, Is currently dark: $isCurrentlyDark")
            
            if (shouldBeDark != isCurrentlyDark) {
                android.util.Log.d("MainActivity", "Theme mismatch detected, reapplying theme")
                ThemeManager.applyTheme(currentThemeMode)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error ensuring theme applied", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        bottomBar.setupWithNavController(menu!!, navController)
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return true
    }
    

    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle result from CameraActivity
        if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Image processing completed, stay in dashboard fragment
            Log.d("MainActivity", "Image processing completed, staying in dashboard fragment")
            
            // No need to navigate, just stay where we are
            // The dialog animation will show and then return to dashboard
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    
    private fun handleNotificationIntent(intent: Intent) {
        try {
            val openHistory = intent.getBooleanExtra("open_history", false)
            val itemId = intent.getLongExtra("item_id", -1L)
            
            if (openHistory) {
                Log.d("MainActivity", "Opening history from notification, itemId: $itemId")
                
                // Navigate to history fragment (Riwayat)
                navController.navigate(R.id.navigation_riwayat)
                
                // Update bottom bar selection
                bottomBar.itemActiveIndex = 1 // Index 1 = Riwayat
                
                Log.d("MainActivity", "Navigated to history fragment successfully")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling notification intent", e)
        }
    }
    
    companion object {
        const val CAMERA_ACTIVITY_REQUEST_CODE = 1001
    }
}
