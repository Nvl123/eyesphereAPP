package com.dicoding.eyesphere_nav.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dicoding.eyesphere_nav.ui.dashboard.DashboardFragment
import com.dicoding.eyesphere_nav.ui.History.HistoryFragment
import com.dicoding.eyesphere_nav.ui.panduan.PanduanFragment

class FragmentPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> HistoryFragment()
            2 -> PanduanFragment()
            else -> DashboardFragment()
        }
    }
}

