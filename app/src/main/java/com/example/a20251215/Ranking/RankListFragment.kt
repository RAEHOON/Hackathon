package com.example.a20251215.Ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a20251215.R

class RankListFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private val adapter = RankListAdapter()

    private val rankType: RankType by lazy {
        val v = requireArguments().getString(ARG_TYPE, RankType.BEST.name)
        RankType.valueOf(v)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rank_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvRank)
        tvEmpty = view.findViewById(R.id.tvEmptyRank)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // ✅ 지금은 예시(더미). 나중에 서버 응답으로 raw만 바꿔 끼우면 됨.
        val raw = loadDummyScores()

        render(raw)
    }

    private fun render(raw: List<UserScore>) {
        val items = buildRankItems(raw, rankType)

        if (items.isEmpty()) {
            // ✅ 아무도 인증 안 했을 때
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
            adapter.submitList(items)
        }
    }

    // ✅ 테스트용 더미
    private fun loadDummyScores(): List<UserScore> {
        return listOf(
            UserScore(1, "미리동걸음왕", 10),
            UserScore(2, "튼튼한다리", 10),
            UserScore(3, "조깅왕", 8),
            UserScore(4, "타잔", 0),
            UserScore(5, "스피드", 0),
        )
        // 전부 0으로 바꾸면 → "이달의 공부왕을 도전해보세요!" 만 뜸
    }

    companion object {
        private const val ARG_TYPE = "arg_type"

        fun newInstance(type: RankType) = RankListFragment().apply {
            arguments = Bundle().apply { putString(ARG_TYPE, type.name) }
        }
    }
}
