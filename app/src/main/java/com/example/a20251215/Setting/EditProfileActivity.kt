package com.example.a20251215.Setting

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.Retrofit.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var tvCurrentNickname: TextView
    private lateinit var etNewNickname: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView

    private var memberId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // View binding
        tvCurrentNickname = findViewById(R.id.tvCurrentNickname)
        etNewNickname = findViewById(R.id.etNewNickname)
        btnSave = findViewById(R.id.btnSaveNickname)
        btnBack = findViewById(R.id.btnBack)

        val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val currentNickname = sharedPref.getString("nickname", "") ?: ""
        memberId = sharedPref.getInt("member_id", -1)

        tvCurrentNickname.text = currentNickname

        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 저장 버튼
        btnSave.setOnClickListener {
            val newNickname = etNewNickname.text.toString().trim()

            if (newNickname.isEmpty()) {
                Toast.makeText(this, "새 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newNickname == currentNickname) {
                Toast.makeText(this, "현재 닉네임과 동일합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            RetrofitClient.apiService.checkNicknameDuplicate(newNickname)
                .enqueue(object : Callback<ApiResponse> {

                    override fun onResponse(
                        call: Call<ApiResponse>,
                        response: Response<ApiResponse>
                    ) {
                        val body = response.body()

                        if (!response.isSuccessful || body == null) {
                            Toast.makeText(this@EditProfileActivity, "서버 응답 오류", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (!body.success) {

                            Toast.makeText(this@EditProfileActivity, body.message, Toast.LENGTH_SHORT).show()
                            return
                        }


                        RetrofitClient.apiService.updateNickname(memberId, newNickname)
                            .enqueue(object : Callback<ApiResponse> {

                                override fun onResponse(
                                    call: Call<ApiResponse>,
                                    response: Response<ApiResponse>
                                ) {
                                    val updateBody = response.body()

                                    if (response.isSuccessful && updateBody?.success == true) {
                                        // SharedPreferences 업데이트
                                        sharedPref.edit()
                                            .putString("nickname", newNickname)
                                            .apply()

                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "닉네임이 변경되었습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            updateBody?.message ?: "닉네임 변경 실패",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                    Toast.makeText(this@EditProfileActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@EditProfileActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
