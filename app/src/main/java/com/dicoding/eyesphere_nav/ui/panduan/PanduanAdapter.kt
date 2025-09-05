package com.dicoding.eyesphere_nav.ui.panduan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.data.PanduanHorizontal

class PanduanAdapter (private val listPanduan: ArrayList<PanduanHorizontal>) : RecyclerView.Adapter<PanduanAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_horizontal, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int
    ): Unit {
        val panduan = listPanduan[position]
        holder.imgPhoto.setImageResource(panduan.pic)
        holder.tvDescription.text = panduan.description
    }

    override fun getItemCount(): Int = listPanduan.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.iv_horizon)
        val tvDescription : TextView = itemView.findViewById(R.id.tv_horizontal)
    }
}