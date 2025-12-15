package com.example.a20251215.Ranking

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RankListFragment : Fragment() {

    companion object {
        private const val TAG = "RankListFragment"
        private const val ARG_TYPE = "arg_type" // BEST / WORST

        fun newInstance(type: String) = RankListFragment().apply {
            arguments = bundleOf(ARG_TYPE to type)
        }
    }

    private var call: Call<RankingResponse>? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvEmpty: TextView

    private val adapter = RankListAdapter()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()

        val root = FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        recyclerView = RecyclerView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(ctx)
            adapter = this@RankListFragment.adapter
        }

        progress = ProgressBar(ctx).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            visibility = View.GONE
        }

        tvEmpty = TextView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            text = "데이터가 없어요"
            textSize = 14f
            visibility = View.GONE
        }

        root.addView(recyclerView)
        root.addView(progress)
        root.addView(tvEmpty)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = requireArguments().getString(ARG_TYPE, "BEST")
        val monthParam = currentMonthParam()

        fetchRanking(type = type, month = monthParam)
    }

    private fun currentMonthParam(): String {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1

        // ✅ 서버가 "12"를 원하면 이거
        return String.format("%02d", month)

        // ✅ 만약 서버가 "2025-12" 같은 형식을 원하면 위 return 대신 이걸로 바꿔
        // val year = cal.get(Calendar.YEAR)
        // return String.format("%04d-%02d", year, month)
    }

    private fun fetchRanking(type: String, month: String) {
        if (!isAdded) return
        showLoading(true)
        tvEmpty.visibility = View.GONE

        call?.cancel()

        call = if (type == "BEST") {
            RetrofitClient.apiService.getBestRanking(month)
        } else {
            RetrofitClient.apiService.getWorstRanking(month)
        }

        val req = call?.request()
        Log.d(TAG, "REQUEST type=$type month=$month url=${req?.url}")

        call?.enqueue(object : Callback<RankingResponse> {
            override fun onResponse(call: Call<RankingResponse>, response: Response<RankingResponse>) {
                if (!isAdded) return
                showLoading(false)

                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    showEmpty("불러오기 실패 (HTTP ${response.code()})")
                    return
                }

                if (!body.success) {
                    showEmpty(body.message.ifBlank { "불러오기 실패" })
                    return
                }

                val list = body.data
                if (list.isEmpty()) {
                    showEmpty("랭킹 데이터가 없어요")
                    return
                }

                adapter.submit(type = type, items = list)
                tvEmpty.visibility = View.GONE
            }

            override fun onFailure(call: Call<RankingResponse>, t: Throwable) {
                if (!isAdded) return
                showLoading(false)
                showEmpty("네트워크 오류: ${t.message ?: "unknown"}")
            }
        })
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmpty(msg: String) {
        adapter.submit(type = "BEST", items = emptyList())
        tvEmpty.text = msg
        tvEmpty.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        call?.cancel()
        call = null
    }
}
