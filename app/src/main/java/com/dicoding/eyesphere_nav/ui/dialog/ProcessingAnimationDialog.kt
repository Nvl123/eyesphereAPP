package com.dicoding.eyesphere_nav.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.databinding.DialogProcessingAnimationBinding

class ProcessingAnimationDialog : DialogFragment() {
    
    private var _binding: DialogProcessingAnimationBinding? = null
    private val binding get() = _binding!!
    
    private var onAnimationComplete: (() -> Unit)? = null
    private var displayDuration: Long = 3000 // Default 3 seconds
    
    companion object {
        private const val ARG_ANIMATION_DURATION = "animation_duration"
        
        fun newInstance(duration: Long = 3000L): ProcessingAnimationDialog {
            return ProcessingAnimationDialog().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ANIMATION_DURATION, duration)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
        arguments?.let {
            displayDuration = it.getLong(ARG_ANIMATION_DURATION, 3000L)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProcessingAnimationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set dialog to full screen
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        
                                    // Load static drawable
        try {
            // Option 1: Use mascot PNG (static image)
            binding.ivProcessingGif.setImageResource(R.drawable.mascot)
            
            // Option 2: Use GIF for animation (uncomment line below if you want GIF animation)
            // binding.ivProcessingGif.setImageResource(R.drawable.eyesp)
            
            // Option 3: Use vector drawable (uncomment line below if you want vector)
            // binding.ivProcessingGif.setImageResource(R.drawable.eyesp_gif)
        } catch (e: Exception) {
            // Log error but continue with fallback
            e.printStackTrace()
            // Fallback to mascot if other resources fail
            binding.ivProcessingGif.setImageResource(R.drawable.mascot)
        }
        
        // Update text with localized strings
        binding.tvProcessingTitle.text = getString(R.string.memproses_gambar_title)
        binding.tvProcessingMessage.text = getString(R.string.memproses_gambar_message)
        
        // Set timer to complete display
        binding.root.postDelayed({
            onAnimationComplete?.invoke()
            dismiss()
        }, displayDuration)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun setOnAnimationCompleteListener(listener: () -> Unit) {
        onAnimationComplete = listener
    }
}
