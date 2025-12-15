package com.example.a20251215.Ranking

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankingPagerAdapter(
    fragment: Fragment,
    private val titles: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        // "BEST" or "WORST"
        return RankListFragment.newInstance(titles[position])
    }
}
