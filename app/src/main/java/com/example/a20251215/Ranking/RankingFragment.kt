package com.example.a20251215.Ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.a20251215.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RankingFragment : Fragment() {

    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ranking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = view.findViewById<TabLayout>(R.id.tabRanking)
        val pager = view.findViewById<ViewPager2>(R.id.pagerRanking)

        pager.adapter = RankingPagerAdapter(this)

        mediator = TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = if (pos == 0) "BEST" else "WORST"
        }.also { it.attach() }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        super.onDestroyView()
    }
}
