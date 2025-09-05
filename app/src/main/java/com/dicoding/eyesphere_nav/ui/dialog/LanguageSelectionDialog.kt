package com.dicoding.eyesphere_nav.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.utils.LanguageManager

class LanguageSelectionDialog : DialogFragment() {
    
    private var onLanguageSelectedListener: OnLanguageSelectedListener? = null
    
    interface OnLanguageSelectedListener {
        fun onLanguageSelected(languageCode: String)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val languages = LanguageManager.getSupportedLanguages()
        val languageNames = languages.map { it.second }
        
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            languageNames
        )
        
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language_selection))
            .setAdapter(adapter) { _, which ->
                val selectedLanguage = languages[which].first
                onLanguageSelectedListener?.onLanguageSelected(selectedLanguage)
                dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null
    }
    
    fun setOnLanguageSelectedListener(listener: OnLanguageSelectedListener) {
        onLanguageSelectedListener = listener
    }
    
    companion object {
        fun newInstance(): LanguageSelectionDialog {
            return LanguageSelectionDialog()
        }
    }
}

