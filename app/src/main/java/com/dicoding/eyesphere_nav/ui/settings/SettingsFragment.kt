package com.dicoding.eyesphere_nav.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dicoding.eyesphere_nav.databinding.FragmentSettingsBinding
import com.dicoding.eyesphere_nav.ui.base.BaseFragment
import com.dicoding.eyesphere_nav.utils.LanguageHelper
import com.dicoding.eyesphere_nav.utils.LanguageManager
import com.dicoding.eyesphere_nav.R

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupLanguageButton()
        setupThemeButtons()
        updateCurrentLanguageDisplay()
    }

    private fun setupLanguageButton() {
        // Set click listeners for all language-related views
        binding.btnBahasa.setOnClickListener {
            showLanguageSelectionDialog()
        }
        
        binding.imgBahasa.setOnClickListener {
            showLanguageSelectionDialog()
        }
        
        binding.tvBahasa.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun setupThemeButtons() {
        // Theme buttons setup (if you have theme functionality)
        // This is just a placeholder - implement based on your theme system
    }

    private fun showLanguageSelectionDialog() {
        LanguageHelper.showLanguageSelectionDialog(this) { languageCode ->
            // Language changed callback
            updateCurrentLanguageDisplay()
            
            // Show success message
            val languageName = LanguageManager.getLanguageDisplayName(languageCode)
            Toast.makeText(
                requireContext(),
                "Language changed to: $languageName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCurrentLanguageDisplay() {
        try {
            val currentLanguage = LanguageManager.getCurrentLanguage(requireContext())
            val languageName = LanguageManager.getLanguageDisplayName(currentLanguage)
            
            // Update UI to show current language
            binding.tvBahasa.text = getString(R.string.bahasa) + ": $languageName"
            
        } catch (e: Exception) {
            // Handle error
            binding.tvBahasa.text = getString(R.string.bahasa)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
