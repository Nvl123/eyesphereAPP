package com.dicoding.eyesphere_nav.ui.panduan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.data.PanduanVertical

class PanduanVerticalAdapter(
    private val listPanduan: ArrayList<PanduanVertical>
) : RecyclerView.Adapter<PanduanVerticalAdapter.ListViewHolder>() {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vertical, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int
    ): Unit {
        try {
            val panduan = listPanduan[position]
            Log.d("PanduanVerticalAdapter", "Binding item $position: ${panduan.title}")
            
            holder.imgPhoto.setImageResource(panduan.pic)
            holder.tvTitle.text = panduan.title
            holder.tvDescription.text = panduan.description
            holder.tvCategory.text = panduan.category
            
            Log.d("PanduanVerticalAdapter", "Item $position bound successfully")
        } catch (e: Exception) {
            Log.e("PanduanVerticalAdapter", "Error binding item $position: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        Log.d("PanduanVerticalAdapter", "getItemCount called: ${listPanduan.size} items")
        return listPanduan.size
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.iv_vertical)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
    }
}

