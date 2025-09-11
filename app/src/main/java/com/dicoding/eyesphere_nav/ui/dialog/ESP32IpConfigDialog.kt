package com.dicoding.eyesphere_nav.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.databinding.DialogEsp32IpConfigBinding
import com.dicoding.eyesphere_nav.utils.ESP32IpManager
import com.dicoding.eyesphere_nav.utils.ESP32ConnectionManager

class ESP32IpConfigDialog : DialogFragment() {
    
    private var _binding: DialogEsp32IpConfigBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var esp32IpManager: ESP32IpManager
    private lateinit var esp32ConnectionManager: ESP32ConnectionManager
    
    interface OnIpConfiguredListener {
        fun onIpConfigured(newIp: String)
    }
    
    private var listener: OnIpConfiguredListener? = null
    
    companion object {
        fun newInstance(): ESP32IpConfigDialog {
            return ESP32IpConfigDialog()
        }
    }
    
    fun setOnIpConfiguredListener(listener: OnIpConfiguredListener) {
        this.listener = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        esp32IpManager = ESP32IpManager.getInstance()
        esp32ConnectionManager = ESP32ConnectionManager.getInstance()
        
        _binding = DialogEsp32IpConfigBinding.inflate(LayoutInflater.from(requireContext()))
        
        // Set current IP address
        val currentIp = esp32IpManager.getIpAddress(requireContext())
        binding.etIpAddress.setText(currentIp)
        binding.etIpAddress.setSelection(currentIp.length)
        
        // Add text watcher for real-time validation
        binding.etIpAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateIpAddress(s.toString())
            }
        })
        
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.ip_config_title))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.ip_config_save)) { _, _ ->
                saveIpAddress()
            }
            .setNegativeButton(getString(R.string.ip_config_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Reset") { _, _ ->
                resetToDefault()
            }
            .create()
    }
    
    private fun validateIpAddress(ipAddress: String) {
        val isValid = esp32IpManager.isValidIpAddress(ipAddress)
        
        if (ipAddress.isNotEmpty() && !isValid) {
            binding.etIpAddress.error = getString(R.string.ip_config_invalid)
        } else {
            binding.etIpAddress.error = null
        }
    }
    
    private fun saveIpAddress() {
        val ipAddress = binding.etIpAddress.text.toString().trim()
        
        if (ipAddress.isEmpty()) {
            showToast(getString(R.string.ip_config_invalid))
            return
        }
        
        if (!esp32IpManager.isValidIpAddress(ipAddress)) {
            showToast(getString(R.string.ip_config_invalid))
            return
        }
        
        val success = esp32IpManager.saveIpAddress(requireContext(), ipAddress)
        if (success) {
            showToast(getString(R.string.ip_config_saved))
            listener?.onIpConfigured(ipAddress)
            
            // Restart connection checking with new IP
            esp32ConnectionManager.stopConnectionCheck()
            esp32ConnectionManager.startConnectionCheck(requireContext())
            
            // Close dialog
            dismiss()
        } else {
            showToast(getString(R.string.ip_config_error))
        }
    }
    
    private fun resetToDefault() {
        val success = esp32IpManager.resetToDefault(requireContext())
        if (success) {
            val defaultIp = esp32IpManager.getIpAddress(requireContext())
            
            // Update the input field to show the reset IP
            binding.etIpAddress.setText(defaultIp)
            binding.etIpAddress.setSelection(defaultIp.length)
            
            showToast("Reset to default IP: $defaultIp")
            listener?.onIpConfigured(defaultIp)
            
            // Restart connection checking with default IP
            esp32ConnectionManager.stopConnectionCheck()
            esp32ConnectionManager.startConnectionCheck(requireContext())
        } else {
            showToast(getString(R.string.ip_config_error))
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}