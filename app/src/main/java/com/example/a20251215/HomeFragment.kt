package com.example.a20251215

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a20251215.Post.WritePostActivity
import com.example.a20251215.Ranking.RankingResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {


    private lateinit var rvCertUsers: RecyclerView
    private lateinit var adapter: CertUserAdapter
    private val userList = ArrayList<com.example.a20251215.Ranking.RankingItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCertUsers = view.findViewById(R.id.rvCertUsers)
        rvCertUsers.layoutManager = LinearLayoutManager(context)

        adapter = CertUserAdapter(userList) { memberId ->
            Toast.makeText(context, "유저 ID $memberId 의 상세 목록을 로드해야 합니다.", Toast.LENGTH_SHORT).show()


        }
        rvCertUsers.adapter = adapter

        // 글쓰기 버튼
        val fabAddCert = view.findViewById<FloatingActionButton>(R.id.fabAddCert)
        fabAddCert.setOnClickListener {
            val intent = Intent(requireContext(), WritePostActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getUsersToday()
    }

    private fun getUsersToday() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        RetrofitClient.apiService.getUsersByDate(today).enqueue(object : Callback<RankingResponse> {
            override fun onResponse(call: Call<RankingResponse>, response: Response<RankingResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data

                    if (!data.isNullOrEmpty()) {
                        adapter.updateList(data)
                    } else {
                        adapter.updateList(emptyList())
                    }
                } else {
                    Log.e("HomeFragment", "사용자 조회 실패: ${response.body()?.message}")
                }
            }

            override fun onFailure(call: Call<RankingResponse>, t: Throwable) {
                Log.e("HomeFragment", "서버 통신 실패: ${t.message}")
            }
        })
    }
}