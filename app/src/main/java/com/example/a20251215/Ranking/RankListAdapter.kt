package com.example.a20251215.Ranking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a20251215.R

class RankListAdapter : RecyclerView.Adapter<RankListAdapter.VH>() {

    private val items = mutableListOf<RankItem>()

    fun submitList(newItems: List<RankItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rank_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvBadge.text = item.badge
        holder.tvName.text = item.name
        holder.tvCount.text = item.count
        holder.tvSub.text = item.sub
    }


    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val tvSub: TextView = itemView.findViewById(R.id.tvSub)
    }
}
