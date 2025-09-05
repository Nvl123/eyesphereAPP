package com.dicoding.eyesphere_nav.ui.History

import com.dicoding.eyesphere_nav.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.eyesphere_nav.data.database.ClassificationResult
import com.dicoding.eyesphere_nav.data.database.DatabaseHelper
import com.dicoding.eyesphere_nav.databinding.FragmentHistoryBinding
import com.dicoding.eyesphere_nav.ml.ClassificationQueueService
import com.dicoding.eyesphere_nav.ui.base.BaseFragment
import com.dicoding.eyesphere_nav.ui.settings.SettingActivity

class HistoryFragment : BaseFragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var databaseHelper: DatabaseHelper


    private val classificationReceiver = object : BroadcastReceiver() {


        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                when (intent?.action) {
                    ClassificationQueueService.ACTION_PROGRESS_UPDATE,
                    ClassificationQueueService.ACTION_ITEM_COMPLETED -> {
                        Log.d(TAG, "Classification update received: ${intent.action}")
                        // Refresh the history list when classification updates
                        loadHistoryData()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in classification broadcast receiver", e)
            }
        }
    }

    companion object {
        private const val TAG = "HistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        initializeDatabase()
        registerBroadcastReceiver()


        binding.topAppbar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.navigation_setting -> {

                    val intent  = Intent(requireContext(), SettingActivity::class.java)
                    startActivity(intent)

                    true
                }
                else -> false
            }
        }

    }
    
    override fun onResume() {
        super.onResume()
        loadHistoryData()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList())
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun initializeDatabase() {
        databaseHelper = DatabaseHelper(requireContext())
    }
    
    private fun registerBroadcastReceiver() {
        try {
            val intentFilter = IntentFilter().apply {
                addAction(ClassificationQueueService.ACTION_PROGRESS_UPDATE)
                addAction(ClassificationQueueService.ACTION_ITEM_COMPLETED)
            }
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(classificationReceiver, intentFilter)
            Log.d(TAG, "Broadcast receiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering broadcast receiver", e)
        }
    }
    
    private fun loadHistoryData() {
        try {
            val classificationResults = databaseHelper.getAllClassificationResults()
            
            if (classificationResults.isEmpty()) {
                // Show empty state
                binding.rvHistory.visibility = View.GONE
                // You can add an empty state view here if needed
                Log.d(TAG, "No classification results found")
            } else {
                binding.rvHistory.visibility = View.VISIBLE
                historyAdapter.updateData(classificationResults)
                Log.d(TAG, "Loaded ${classificationResults.size} classification results")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading history data", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(classificationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering broadcast receiver", e)
        }
        _binding = null
    }
}