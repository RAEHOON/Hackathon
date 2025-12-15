package com.example.a20251215.Ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.a20251215.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar

class RankingFragment : Fragment() {

    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ranking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvRankSub)?.let { tv ->
            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
            tv.text = "${month}월 랭킹"
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.tabRanking)
        val viewPager = view.findViewById<ViewPager2>(R.id.pagerRanking)

        val titles = listOf("BEST", "WORST")
        viewPager.adapter = RankingPagerAdapter(this, titles)

        mediator = TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.also { it.attach() }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        super.onDestroyView()
    }
}
