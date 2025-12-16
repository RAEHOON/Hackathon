package com.example.a20251215

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.a20251215.Ranking.RankingResponse
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlarmFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }
    /*
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val listContainer = view.findViewById<LinearLayout>(R.id.listContainer)

           val date = "2025-12-15"
            RetrofitClient.apiService.getUsersByDate(date).enqueue(object : Callback<RankingResponse> {
                override fun onResponse(
                    call: Call<RankingResponse>,
                    response: Response<RankingResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val users = response.body()?.data ?: emptyList()

                        for ((index, user) in users.withIndex()) {
                            val tv = TextView(requireContext()).apply {
                                text = "${index + 1}. ${user.nickname} (${user.uploadCount}개)"
                                setTextColor(android.graphics.Color.WHITE)
                                textSize = 16f
                                setPadding(0, 10, 0, 10)
                            }
                            listContainer.addView(tv)
                        }
                    } else {
                        Toast.makeText(requireContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RankingResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }*/
}
