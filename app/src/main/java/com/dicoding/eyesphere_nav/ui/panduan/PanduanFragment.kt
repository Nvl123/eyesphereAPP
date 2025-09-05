package com.dicoding.eyesphere_nav.ui.panduan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.data.PanduanHorizontal
import com.dicoding.eyesphere_nav.data.PanduanVertical
import com.dicoding.eyesphere_nav.databinding.FragmentPanduanUltraCompactBinding
import com.dicoding.eyesphere_nav.ui.base.BaseFragment
import com.dicoding.eyesphere_nav.ui.settings.SettingActivity
import androidx.recyclerview.widget.RecyclerView

class PanduanFragment : BaseFragment() {

    private var _binding: FragmentPanduanUltraCompactBinding? = null
    private val binding get() = _binding!!
    private val panduanHorizontalList = ArrayList<PanduanHorizontal>()
    private val panduanVerticalList = ArrayList<PanduanVertical>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanduanUltraCompactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupTopAppBar()
            setupHorizontalRecyclerView()
            setupVerticalRecyclerView()
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    private fun setupTopAppBar() {
        try {
            binding.topAppbar?.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_setting -> {
                        val intent = Intent(requireContext(), SettingActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error setting up top app bar: ${e.message}", e)
        }
    }

    private fun setupHorizontalRecyclerView() {
        try {
            // Clear existing list to avoid duplicates
            panduanHorizontalList.clear()
            panduanHorizontalList.addAll(getListPanduanHorizontal())
            
            binding.rvHorizontal?.let { recyclerView ->
                recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                val panduanAdapter = PanduanAdapter(panduanHorizontalList)
                recyclerView.adapter = panduanAdapter
            } ?: run {
                Log.e("PanduanFragment", "rvHorizontal is null")
            }
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error setting up horizontal recycler view: ${e.message}", e)
        }
    }

    private fun setupVerticalRecyclerView() {
        try {
            // Clear existing list to avoid duplicates
            panduanVerticalList.clear()
            panduanVerticalList.addAll(getListPanduanVertical())
            
            binding.rvVertical?.let { recyclerView ->
                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                
                // Enable gap handling for better spacing
                layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE)
                
                recyclerView.layoutManager = layoutManager
                
                // Add custom Pinterest-style item decoration
                recyclerView.addItemDecoration(PinterestItemDecoration(1))
                
                val panduanVerticalAdapter = PanduanVerticalAdapter(panduanVerticalList)
                recyclerView.adapter = panduanVerticalAdapter
                
                Log.d("PanduanFragment", "Vertical RecyclerView setup completed with ${panduanVerticalList.size} items")
            } ?: run {
                Log.e("PanduanFragment", "rvVertical is null")
            }
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error setting up vertical recycler view: ${e.message}", e)
        }
    }

    private fun getListPanduanHorizontal(): List<PanduanHorizontal> {
        val listPanduan = ArrayList<PanduanHorizontal>()
        
        try {
            val dataImg = resources.obtainTypedArray(R.array.photo_panduan)
            val dataDesc = resources.getStringArray(R.array.text_panduan)
            
            if (dataImg.length() == 0 || dataDesc.isEmpty()) {
                Log.w("PanduanFragment", "Empty horizontal arrays found")
                dataImg.recycle()
                return listPanduan
            }
            
            // Ensure both arrays have the same length to avoid index out of bounds
            val minLength = minOf(dataImg.length(), dataDesc.size)
            
            for (i in 0 until minLength) {
                try {
                    val imgRes = dataImg.getResourceId(i, -1)
                    val desc = dataDesc[i]
                    
                    if (imgRes != -1 && desc.isNotEmpty()) {
                        listPanduan.add(PanduanHorizontal(imgRes, desc))
                    } else {
                        Log.w("PanduanFragment", "Invalid horizontal resource at index $i: imgRes=$imgRes, desc='$desc'")
                    }
                } catch (e: Exception) {
                    Log.e("PanduanFragment", "Error adding horizontal item at index $i: ${e.message}", e)
                }
            }
            
            dataImg.recycle()
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error getting horizontal panduan list: ${e.message}", e)
        }
        
        return listPanduan
    }

    private fun getListPanduanVertical(): List<PanduanVertical> {
        val listPanduan = ArrayList<PanduanVertical>()
        
        try {
            Log.d("PanduanFragment", "Getting vertical panduan list...")
            
            val dataImg = resources.obtainTypedArray(R.array.photo_panduan_vertical)
            val dataTitle = resources.getStringArray(R.array.title_panduan_vertical)
            val dataDesc = resources.getStringArray(R.array.description_panduan_vertical)
            val dataCategory = resources.getStringArray(R.array.category_panduan_vertical)
            
            Log.d("PanduanFragment", "Array lengths - Images: ${dataImg.length()}, Titles: ${dataTitle.size}, Descriptions: ${dataDesc.size}, Categories: ${dataCategory.size}")
            
            if (dataImg.length() == 0 || dataTitle.isEmpty() || dataDesc.isEmpty() || dataCategory.isEmpty()) {
                Log.w("PanduanFragment", "Empty vertical arrays found")
                dataImg.recycle()
                return listPanduan
            }
            
            // Ensure all arrays have the same length to avoid index out of bounds
            val minLength = minOf(dataImg.length(), dataTitle.size, dataDesc.size, dataCategory.size)
            Log.d("PanduanFragment", "Processing $minLength items")
            
            for (i in 0 until minLength) {
                try {
                    val imgRes = dataImg.getResourceId(i, -1)
                    val title = dataTitle[i]
                    val desc = dataDesc[i]
                    val category = dataCategory[i]
                    
                    if (imgRes != -1 && title.isNotEmpty() && desc.isNotEmpty() && category.isNotEmpty()) {
                        listPanduan.add(PanduanVertical(imgRes, title, desc, category))
                        Log.d("PanduanFragment", "Added vertical item $i: $title")
                    } else {
                        Log.w("PanduanFragment", "Invalid vertical resource at index $i: imgRes=$imgRes, title='$title', desc='$desc', category='$category'")
                    }
                } catch (e: Exception) {
                    Log.e("PanduanFragment", "Error adding vertical item at index $i: ${e.message}", e)
                }
            }
            
            dataImg.recycle()
            Log.d("PanduanFragment", "Vertical panduan list created with ${listPanduan.size} items")
        } catch (e: Exception) {
            Log.e("PanduanFragment", "Error getting vertical panduan list: ${e.message}", e)
        }
        
        return listPanduan
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}