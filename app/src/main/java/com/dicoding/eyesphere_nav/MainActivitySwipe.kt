package com.dicoding.eyesphere_nav

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import me.ibrahimsn.lib.SmoothBottomBar
import com.dicoding.eyesphere_nav.databinding.ActivityMainSwipeBinding
import com.dicoding.eyesphere_nav.ui.adapter.FragmentPagerAdapter
import com.dicoding.eyesphere_nav.utils.ThemeManager

class MainActivitySwipe : AppCompatActivity() {

    private lateinit var binding: ActivityMainSwipeBinding
    private lateinit var bottomBar: SmoothBottomBar
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme before setting content view
        applySavedTheme()

        binding = ActivityMainSwipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewPager2
        viewPager = binding.viewPager
        val pagerAdapter = FragmentPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Initialize SmoothBottomBar
        bottomBar = binding.navView

        // Setup ViewPager with BottomNavigation
        setupViewPagerWithBottomNav()

        // Setup bottom navigation menu
        setupBottomNavigation()
    }

    private fun applySavedTheme() {
        try {
            val savedThemeMode = ThemeManager.getCurrentThemeMode(this)
            ThemeManager.applyTheme(savedThemeMode)
        } catch (e: Exception) {
            // Fallback to system theme if there's an error
            ThemeManager.applyTheme(ThemeManager.THEME_SYSTEM)
        }
    }

    private fun setupViewPagerWithBottomNav() {
        // Sync ViewPager with BottomNavigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Update bottom navigation when page changes
                updateBottomNavigation(position)
            }
        })
    }

    private fun updateBottomNavigation(position: Int) {
        // For now, just let the ViewPager handle navigation
        // SmoothBottomBar will be updated through user clicks
        // This avoids complex reflection issues with different library versions
        
        // You can add custom logic here later if needed
        // For example: update UI elements, show indicators, etc.
    }

    private fun setupBottomNavigation() {
        // Handle bottom navigation item clicks
        // SmoothBottomBar uses position-based selection (0, 1, 2)
        bottomBar.setOnItemSelectedListener { position ->
            // Navigate ViewPager to the selected position
            viewPager.setCurrentItem(position, true)
            true // Return true to indicate selection was handled
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        return true
    }

    // Enable/disable swipe based on current fragment
    fun setSwipeEnabled(enabled: Boolean) {
        viewPager.isUserInputEnabled = enabled
    }
}
