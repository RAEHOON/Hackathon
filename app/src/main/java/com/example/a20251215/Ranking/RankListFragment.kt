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
import com.example.a20251215.Retrofit.RetrofitClient
import org.threeten.bp.YearMonth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RankListFragment : Fragment() {


    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private val adapter = RankListAdapter()

    private var callRanking: Call<RankingResponse>? = null

    private val rankType: RankType by lazy {
        RankType.valueOf(requireArguments().getString(ARG_TYPE, RankType.BEST.name))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rank_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvRank)
        tvEmpty = view.findViewById(R.id.tvEmptyRank)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadRanking()
    }

    private fun loadRanking() {
        showMessage("불러오는 중...")

        val monthParam = YearMonth.now().toString() // "2025-12"

        callRanking?.cancel()
        callRanking = when (rankType) {
            RankType.BEST -> RetrofitClient.apiService.getBestRanking(monthParam)
            RankType.WORST -> RetrofitClient.apiService.getWorstRanking(monthParam)
        }

        callRanking?.enqueue(object : Callback<RankingResponse> {
            override fun onResponse(call: Call<RankingResponse>, response: Response<RankingResponse>) {
                if (!isAdded) return

                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    showMessage("불러오기 실패 (HTTP ${response.code()})")
                    return
                }
                if (!body.success) {
                    showMessage(body.message.ifBlank { "불러오기 실패" })
                    return
                }

                 val raw = body.data.map {
                    UserScore(
                        userId = it.memberId,
                        name = it.nickname,
                        certCount = it.uploadCount
                    )
                }

                 val items = buildRankItems(raw, rankType)

                if (items.isEmpty()) {
                    showMessage("이달의 공부왕을 도전해보세요!")
                } else {
                    tvEmpty.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submitList(items)
                }
            }

            override fun onFailure(call: Call<RankingResponse>, t: Throwable) {
                if (!isAdded) return
                showMessage("네트워크 오류: ${t.message ?: "unknown"}")
            }
        })
    }

    private fun showMessage(msg: String) {
        tvEmpty.visibility = View.VISIBLE
        rv.visibility = View.GONE
        tvEmpty.text = msg
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callRanking?.cancel()
        callRanking = null
    }

    companion object {
        private const val ARG_TYPE = "arg_type"

        fun newInstance(type: RankType) = RankListFragment().apply {
            arguments = Bundle().apply { putString(ARG_TYPE, type.name) }
        }
    }
}
