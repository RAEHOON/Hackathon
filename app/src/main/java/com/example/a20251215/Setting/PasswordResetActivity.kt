package com.example.a20251215.Setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.LoginActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.ValidationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var etCurrentPw: TextInputEditText
    private lateinit var etNewPw: TextInputEditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePw: MaterialButton

    private var memberId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        val sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        memberId = sharedPref.getInt("member_id", -1)

        etCurrentPw = findViewById(R.id.etCurrentPw)
        etNewPw = findViewById(R.id.etNewPw)
        btnBack = findViewById(R.id.btnBack)
        btnChangePw = findViewById(R.id.btnChangePw)

        btnBack.setOnClickListener {
            finish()
        }

        btnChangePw.setOnClickListener {
            val currentPw = etCurrentPw.text.toString().trim()
            val newPw = etNewPw.text.toString().trim()

            // 입력 유효성 검사
            if (currentPw.isEmpty() || newPw.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.PW_PATTERN.matcher(newPw).matches()) {
                Toast.makeText(this, "비밀번호는 8~20자, 영문/숫자/특수문자 포함해야 합니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            RetrofitClient.apiService.resetPassword(memberId, currentPw, newPw)
                .enqueue(object : Callback<com.example.a20251215.Retrofit.ApiResponse> {
                    override fun onResponse(
                        call: Call<com.example.a20251215.Retrofit.ApiResponse>,
                        response: Response<com.example.a20251215.Retrofit.ApiResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // SharedPreferences 초기화 (자동 로그인 방지)
                            sharedPref.edit().clear().apply()

                            Toast.makeText(this@PasswordResetActivity, "비밀번호가 변경되었습니다. 다시 로그인하세요.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@PasswordResetActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@PasswordResetActivity, response.body()?.message ?: "변경 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<com.example.a20251215.Retrofit.ApiResponse>, t: Throwable) {
                        Toast.makeText(this@PasswordResetActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
