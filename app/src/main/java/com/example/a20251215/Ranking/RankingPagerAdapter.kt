package com.example.a20251215.Ranking

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            RankListFragment.newInstance(RankType.BEST)
        } else {
            RankListFragment.newInstance(RankType.WORST)
        }
    }
}
