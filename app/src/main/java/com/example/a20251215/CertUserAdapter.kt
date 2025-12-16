package com.example.a20251215

import com.example.a20251215.Post.Post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a20251215.Ranking.RankingItem

class CertUserAdapter (private var userList: List<RankingItem>, private val itemClickListener: (Int) -> Unit) : RecyclerView.Adapter<CertUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_home_item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userList[position]

        holder.tvName.text = item.nickname
        holder.tvTitle.text = "인증 횟수: ${item.uploadCount}회"


        holder.itemView.setOnClickListener {
            itemClickListener(item.memberId)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<RankingItem>) {
        userList = newList
        notifyDataSetChanged()
    }
}