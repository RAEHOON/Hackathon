package com.example.a20251215.Ranking

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankListAdapter : RecyclerView.Adapter<RankListAdapter.VH>() {

    private var type: String = "BEST" // BEST / WORST
    private val items = mutableListOf<RankingItem>()

    fun submit(type: String, items: List<RankingItem>) {
        this.type = type
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        fun dp(v: Int): Int = (v * ctx.resources.displayMetrics.density).toInt()

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

        val tvRank = TextView(ctx).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(44), ViewGroup.LayoutParams.WRAP_CONTENT)
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

        val tvCount = TextView(ctx).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#EDEFF6"))
            alpha = 0.9f
        }

        col.addView(tvNick)
        col.addView(tvSub)

        row.addView(tvRank)
        row.addView(col)
        row.addView(tvCount)

        return VH(row, tvRank, tvNick, tvSub, tvCount)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val rankNum = position + 1

        holder.tvRank.text = when (rankNum) {
            1 -> if (type == "BEST") "🥇" else "😡"
            2 -> if (type == "BEST") "🥈" else "😭"
            3 -> if (type == "BEST") "🥉" else "😮‍💨"
            else -> rankNum.toString()
        }

        holder.tvNick.text = item.nickname
        holder.tvSub.text = if (type == "BEST") "이번 달 BEST $rankNum" else "이번 달 WORST $rankNum"
        holder.tvCount.text = "인증 ${item.uploadCount}회"
    }

    override fun getItemCount(): Int = items.size

    class VH(
        parent: LinearLayout,
        val tvRank: TextView,
        val tvNick: TextView,
        val tvSub: TextView,
        val tvCount: TextView
    ) : RecyclerView.ViewHolder(parent)
}
