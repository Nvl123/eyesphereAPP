package com.dicoding.eyesphere_nav.ui.panduan

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class PinterestItemDecoration(private val spacing: Int = 2) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
        val spanIndex = layoutParams?.spanIndex ?: 0
        
        // Add spacing based on span index for Pinterest-like effect
        when (spanIndex) {
            0 -> {
                // Left column
                outRect.left = spacing
                outRect.right = spacing / 2
            }
            1 -> {
                // Right column
                outRect.left = spacing / 2
                outRect.right = spacing
            }
        }
        
        // Add vertical spacing
        outRect.top = spacing / 2
        outRect.bottom = spacing / 2
    }
}
