package com.dicoding.eyesphere_nav.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.databinding.ActivitySettingBinding
import com.dicoding.eyesphere_nav.utils.ThemeManager
import com.dicoding.eyesphere_nav.utils.PreferencesManager
import com.dicoding.eyesphere_nav.utils.TranslationHelper
import android.util.Log
import com.dicoding.eyesphere_nav.ui.dialog.ESP32IpConfigDialog

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Initialize preferences manager
        preferencesManager = PreferencesManager.getInstance(this)

        // Setup click listeners
        setupClickListeners()
        
        // Update UI based on current theme
        updateThemeUI()
    }

    private fun setupClickListeners() {
        // Top app bar navigation
        binding.topAppbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Dark mode toggle
        binding.btnModeGL.setOnClickListener {
            toggleDarkMode()
        }

        // Language button - Open Android system language settings
        binding.btnBahasa.setOnClickListener {
            openSystemLanguageSettings()
        }
        
        binding.imgBahasa.setOnClickListener {
            openSystemLanguageSettings()
        }
        
        binding.tvBahasa.setOnClickListener {
            openSystemLanguageSettings()
        }

        // FAQ button
        binding.btnFaq.setOnClickListener {
            // TODO: Implement FAQ
            Toast.makeText(this, "Fitur FAQ akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        // ESP32 IP Configuration button
        binding.btnEsp32Config.setOnClickListener {
            showEsp32IpConfigDialog()
        }
        
        binding.imgEsp32Config.setOnClickListener {
            showEsp32IpConfigDialog()
        }
        
        binding.tvEsp32Config.setOnClickListener {
            showEsp32IpConfigDialog()
        }

        // Developer contact button
        binding.btnDeveloper.setOnClickListener {
            // TODO: Implement developer contact
            Toast.makeText(this, "Fitur hubungi pengembang akan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Open Android system language settings
     */
    private fun openSystemLanguageSettings() {
        try {
            // Intent untuk membuka Android system language settings
            val intent = Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS)
            
            // Tambahkan flag untuk membuka sebagai activity baru
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Cek apakah ada app yang bisa handle intent ini
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                
                // Tampilkan pesan informatif
                Toast.makeText(
                    this,
                    "Buka pengaturan bahasa sistem Android. Aplikasi akan menyesuaikan otomatis.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Fallback jika tidak ada app yang bisa handle
                Toast.makeText(
                    this,
                    "Tidak dapat membuka pengaturan bahasa sistem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingActivity", "Error opening language settings: ${e.message}", e)
            Toast.makeText(
                this,
                "Gagal membuka pengaturan bahasa: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleDarkMode() {
        // Toggle theme using ThemeManager
        ThemeManager.toggleTheme(this)
        
        // Update preferences
        val isDark = ThemeManager.isDarkMode(this)
        preferencesManager.isDarkMode = isDark
        preferencesManager.themeMode = if (isDark) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT
        
        // Update UI
        updateThemeUI()
        
        // Show feedback
        val message = if (isDark) "Mode gelap diaktifkan" else "Mode terang diaktifkan"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        // Recreate activity to apply theme changes
        recreate()
    }

    private fun updateThemeUI() {
        val isDark = ThemeManager.isDarkMode(this)
        
        // Update status text
        val statusText = if (isDark) "Off" else "On"
        binding.tvModeGLStatus.text = statusText
        
        // Update mode text
        val modeText = if (isDark) getString(R.string.mode_gelap) else getString(R.string.mode_terang)
        binding.tvModeGL.text = modeText
        
        // Update icon tint based on theme
        val iconTint = if (isDark) {
            getColor(R.color.text_primary_dark)
        } else {
            getColor(R.color.text_primary_light)
        }
        
        binding.imgModeGL.setColorFilter(iconTint)
        binding.tvModeGL.setTextColor(iconTint)
        binding.tvModeGLStatus.setTextColor(iconTint)
    }

    override fun onResume() {
        super.onResume()
        // Update UI when returning to this activity
        updateThemeUI()
    }
    
    /**
     * Test translation service with sample server response
     */
    private fun testTranslationService() {
        try {
            val sampleServerResponse = "Hasil analisis menunjukkan kondisi mata yang baik. Tidak ada tanda-tanda penyakit serius yang terdeteksi."
            
            if (TranslationHelper.isTranslationAvailable()) {
                Log.d("SettingActivity", "Translation service is available")
                Toast.makeText(this, "Testing translation service...", Toast.LENGTH_SHORT).show()
                
                // Test translation to system language
                TranslationHelper.translateServerResponseAsync(this, sampleServerResponse) { translatedText ->
                    Log.d("SettingActivity", "Translation test successful")
                    Log.d("SettingActivity", "Original: $sampleServerResponse")
                    Log.d("SettingActivity", "Translated: $translatedText")
                    
                    // Show result in UI thread
                    runOnUiThread {
                        Toast.makeText(
                            this@SettingActivity,
                            "Translation successful!\nOriginal: $sampleServerResponse\nTranslated: $translatedText",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Log.w("SettingActivity", "Translation service is not available")
                Toast.makeText(this, "Translation service is not available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SettingActivity", "Error testing translation service", e)
            Toast.makeText(this, "Error testing translation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Show ESP32 IP Configuration Dialog
     */
    private fun showEsp32IpConfigDialog() {
        try {
            val dialog = ESP32IpConfigDialog.newInstance()
            dialog.setOnIpConfiguredListener(object : ESP32IpConfigDialog.OnIpConfiguredListener {
                override fun onIpConfigured(newIp: String) {
                    Toast.makeText(this@SettingActivity, "IP ESP32 berhasil diperbarui: $newIp", Toast.LENGTH_LONG).show()
                }
            })
            dialog.show(supportFragmentManager, "ESP32IpConfigDialog")
        } catch (e: Exception) {
            Log.e("SettingActivity", "Error showing ESP32 IP config dialog", e)
            Toast.makeText(this, "Error membuka konfigurasi IP: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}