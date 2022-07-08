package com.app.tyst.ui.tutorial

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TutorialViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    private val NUM_ITEMS = 6

    override fun getItem(position: Int): Fragment {
        return TutorialFragment.newInstance(position+1)
    }

    override fun getCount(): Int {
        return NUM_ITEMS
    }
}