package com.dicoding.eyesphere_nav.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.dicoding.eyesphere_nav.databinding.FragmentDashboardBinding
import com.dicoding.eyesphere_nav.ui.base.BaseFragment
import com.dicoding.eyesphere_nav.ui.camera.CameraActivity
import com.dicoding.eyesphere_nav.ui.settings.SettingActivity
import com.dicoding.eyesphere_nav.utils.LanguageHelper
import com.dicoding.eyesphere_nav.utils.LanguageManager
import com.dicoding.eyesphere_nav.utils.TranslationHelper
import com.dicoding.eyesphere_nav.utils.ServerResponseProcessor
import com.dicoding.eyesphere_nav.utils.LanguageConsistencyHelper
import android.content.Context
import com.dicoding.eyesphere_nav.MainActivity
import com.dicoding.eyesphere_nav.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dashboardViewModel.text.observe(viewLifecycleOwner) {
        
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if dark mode is enabled and hide the background ImageView accordingly
        checkDarkModeAndUpdateUI()

        binding.btnCamera.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            startActivityForResult(intent, MainActivity.CAMERA_ACTIVITY_REQUEST_CODE)
        }

        binding.btnSetting.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        // Check dark mode again when fragment resumes to handle theme changes
        checkDarkModeAndUpdateUI()
        
        // Update UI language if needed
        updateUILanguage()
    }

    private fun checkDarkModeAndUpdateUI() {
        // Check if dark mode is enabled using multiple methods for reliability
        val isDarkMode = when {
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // Check system theme if using system default
                val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
        
        // Log for debugging
        android.util.Log.d("DashboardFragment", "Dark mode detected: $isDarkMode")
        
        // Hide the background ImageView if dark mode is enabled
        if (isDarkMode) {
            binding.ivBgAlat.visibility = View.GONE
            android.util.Log.d("DashboardFragment", "Background ImageView hidden for dark mode")
        } else {
            binding.ivBgAlat.visibility = View.VISIBLE
            android.util.Log.d("DashboardFragment", "Background ImageView visible for light mode")
        }
    }
    
    /**
     * Update UI language based on current language setting
     * OPTIMIZED: Minimal UI thread operations
     */
    private fun updateUILanguage() {
        try {
            // Check if UI translation is needed using the consistency helper
            // This is now optimized with caching - minimal UI thread load
            if (LanguageConsistencyHelper.shouldTranslateUI(requireContext())) {
                // Log in background to avoid UI blocking
                CoroutineScope(Dispatchers.Default).launch {
                    android.util.Log.d("DashboardFragment", "UI translation needed")
                }
                
                // Translate button text to system language asynchronously
                TranslationHelper.translateServerResponseAsync(requireContext(), getString(R.string.ambil_gambar)) { translatedText ->
                    // Use requireActivity().runOnUiThread for Fragment with safety check
                    try {
                        requireActivity().runOnUiThread {
                            if (isAdded && !isDetached) {
                                binding.btnCamera.text = translatedText
                                // Log in background to avoid UI blocking
                                CoroutineScope(Dispatchers.Default).launch {
                                    android.util.Log.d("DashboardFragment", "Button text updated to translated: $translatedText")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardFragment", "Error updating UI with translated text", e)
                        // Fallback to original string
                        binding.btnCamera.text = getString(R.string.ambil_gambar)
                    }
                }
            } else {
                // Use original Indonesian string if translation not needed
                binding.btnCamera.text = getString(R.string.ambil_gambar)
                // Log in background to avoid UI blocking
                CoroutineScope(Dispatchers.Default).launch {
                    android.util.Log.d("DashboardFragment", "Using original Indonesian text: ${getString(R.string.ambil_gambar)}")
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("DashboardFragment", "Error updating UI language: ${e.message}", e)
            // Fallback to original string
            binding.btnCamera.text = getString(R.string.ambil_gambar)
        }
    }
    
    /**
     * Handle server response with automatic translation and text cleaning
     * This would be called when you receive response from your AI server
     */
    private fun handleServerResponse(indonesianResponse: String) {
        try {
            // Process server response with automatic translation and text cleaning
            ServerResponseProcessor.processServerResponse(
                context = requireContext(),
                indonesianResponse = indonesianResponse,
                onProcessed = { processedResponse ->
                    android.util.Log.d("DashboardFragment", "Original response: ${processedResponse.originalText}")
                    android.util.Log.d("DashboardFragment", "Translated response: ${processedResponse.translatedText}")
                    android.util.Log.d("DashboardFragment", "Cleaned response: ${processedResponse.cleanedText}")
                    android.util.Log.d("DashboardFragment", "Needs translation: ${processedResponse.needsTranslation}")
                    
                    // Use processed response in your UI
                    try {
                        requireActivity().runOnUiThread {
                            if (isAdded && !isDetached) {
                                // Example: Update some UI element with cleaned response
                                // binding.tvServerResponse.text = processedResponse.cleanedText
                                
                                // Store the processed response for later use
                                // This ensures users see the translated and cleaned text immediately
                                storeProcessedResponse(processedResponse)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardFragment", "Error updating UI with processed response", e)
                    }
                },
                onError = { errorMessage ->
                    android.util.Log.e("DashboardFragment", "Error processing server response: $errorMessage")
                    // Fallback to original response
                    try {
                        requireActivity().runOnUiThread {
                            if (isAdded && !isDetached) {
                                // binding.tvServerResponse.text = indonesianResponse
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardFragment", "Error updating UI with fallback response", e)
                    }
                }
            )
            
        } catch (e: Exception) {
            android.util.Log.e("DashboardFragment", "Error handling server response: ${e.message}", e)
            // Fallback to original response
            try {
                requireActivity().runOnUiThread {
                    if (isAdded && !isDetached) {
                        // binding.tvServerResponse.text = indonesianResponse
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardFragment", "Error updating UI with fallback response", e)
            }
        }
    }
    
    /**
     * Store processed response for later use
     * This ensures that translated and cleaned text is available immediately
     */
    private fun storeProcessedResponse(processedResponse: ServerResponseProcessor.ProcessedResponse) {
        try {
            // Here you would store the processed response
            // This could be in SharedPreferences, local database, or memory cache
            // The key is that users don't have to wait for translation when viewing history
            
            android.util.Log.d("DashboardFragment", "Storing processed response")
            android.util.Log.d("DashboardFragment", "Original: ${processedResponse.originalText}")
            android.util.Log.d("DashboardFragment", "Translated: ${processedResponse.translatedText}")
            android.util.Log.d("DashboardFragment", "Cleaned: ${processedResponse.cleanedText}")
            
            // TODO: Implement storage mechanism for processed response
            // Example:
            // - SharedPreferences for simple caching
            // - Local database for persistent storage
            // - Memory cache for immediate access
            
        } catch (e: Exception) {
            android.util.Log.e("DashboardFragment", "Error storing processed response", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}