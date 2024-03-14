package com.cscorner.myclothes.menu.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.manager.Lifecycle

class HomeViewPagerAdapter (
    private val fragments: List<Fragment>,
    fm: FragmentManager,
    lifecycle: androidx.lifecycle.Lifecycle
): FragmentStateAdapter(fm, lifecycle){
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}