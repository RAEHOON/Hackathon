package com.example.a20251215.Ranking

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // 상단 "12월 랭킹"
        view.findViewById<TextView>(R.id.tvRankSub)?.let { tv ->
            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
            tv.text = "${month}월 랭킹"
        }

        val tabs = view.findViewById<TabLayout>(R.id.tabRanking)
        val pager = view.findViewById<ViewPager2>(R.id.pagerRanking)

        pager.adapter = RankPagerAdapter { dp(it) }

        mediator = TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = if (pos == 0) "BEST" else "WORST"
        }.also { it.attach() }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        super.onDestroyView()
    }

    /** ✅ ViewPager2 페이지 1장 = RecyclerView 1개 */
    private inner class RankPagerAdapter(
        private val dp: (Int) -> Int
    ) : RecyclerView.Adapter<RankPagerAdapter.PageVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
            val rv = RecyclerView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                layoutManager = LinearLayoutManager(parent.context)
                setPadding(0, dp(12), 0, dp(12))
                clipToPadding = false
            }
            return PageVH(rv)
        }

        override fun onBindViewHolder(holder: PageVH, position: Int) {
            val list = if (position == 0) bestDummy() else worstDummy()
            holder.recyclerView.adapter = RankListAdapter(list, dp)
        }

        override fun getItemCount(): Int = 2

        inner class PageVH(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)
    }

    /** 더미 데이터(나중에 서버 연동하면 여기만 바꾸면 됨) */
    private fun bestDummy(): List<RankItem> = listOf(
        RankItem("🥇", "미리동걸음왕", "인증 30회", "이번 달 1등"),
        RankItem("🥈", "튼튼한다리", "인증 24회", "이번 달 2등"),
        RankItem("🥉", "조깅왕", "인증 19회", "이번 달 3등"),
        RankItem("4", "타잔", "인증 14회", "이번 달 4등"),
        RankItem("5", "스피드", "인증 10회", "이번 달 5등")
    )

    private fun worstDummy(): List<RankItem> = listOf(
        RankItem("😡", "오늘은 쉬는날", "인증 0회", "이번 달 워스트 1"),
        RankItem("😭", "내일부터진짜", "인증 1회", "이번 달 워스트 2"),
        RankItem("😮‍💨", "작심삼일", "인증 2회", "이번 달 워스트 3"),
        RankItem("4", "핸드폰중독", "인증 3회", "이번 달 워스트 4"),
        RankItem("5", "잠만보", "인증 4회", "이번 달 워스트 5")
    )

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

/** ✅ 랭킹 아이템 */
data class RankItem(
    val badge: String,
    val nickname: String,
    val stat: String,
    val sub: String
)


/** ✅ RecyclerView 어댑터(간단 UI를 코드로 생성) */
class RankListAdapter(
    private val items: List<RankItem>,
    private val dp: (Int) -> Int
) : RecyclerView.Adapter<RankListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = dp(16)
                rightMargin = dp(16)
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val tvBadge = TextView(ctx).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(36), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvNick = TextView(ctx).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
        }

        val tvSub = TextView(ctx).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#AEB6CF"))
        }

        val tvStat = TextView(ctx).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#EDEFF6"))
            alpha = 0.9f
        }

        col.addView(tvNick)
        col.addView(tvSub)

        row.addView(tvBadge)
        row.addView(col)
        row.addView(tvStat)

        return VH(row, tvBadge, tvNick, tvSub, tvStat)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvBadge.text = item.badge
        holder.tvNick.text = item.nickname
        holder.tvSub.text = item.sub
        holder.tvStat.text = item.stat
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        val tvBadge: TextView,
        val tvNick: TextView,
        val tvSub: TextView,
        val tvStat: TextView
    ) : RecyclerView.ViewHolder(itemView)
}
