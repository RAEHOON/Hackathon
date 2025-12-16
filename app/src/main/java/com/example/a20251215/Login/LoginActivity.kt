package com.example.a20251215

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.Find.FindIdActivity
import com.example.a20251215.Find.FindPasswordActivity
import com.example.a20251215.Retrofit.ApiService
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // XML 요소 연결
        val etLoginId = findViewById<EditText>(R.id.etlogin_id)
        val etPassword = findViewById<EditText>(R.id.etpassword)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<TextView>(R.id.signup_btn)

        val findIdBtn = findViewById<TextView>(R.id.find_id_btn)
        val findPasswordBtn = findViewById<TextView>(R.id.find_password_btn)

        // Retrofit API 연결
        apiService = RetrofitClient.apiService

        findIdBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, FindIdActivity::class.java))
            finish()
        }

        findPasswordBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, FindPasswordActivity::class.java))
            finish()
        }

        // 로그인 버튼 클릭
        loginBtn.setOnClickListener {
            val loginId = etLoginId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (loginId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(loginId, password)
        }

        // 회원가입 이동
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser(loginId: String, password: String) {
        apiService.login(loginId, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data!!

                    // SharedPreferences에 정보 저장
                    val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("member_id", user.memberId)
                        putString("login_id", user.loginId)
                        putString("email", user.email)
                        putString("username", user.username)
                        putString("nickname", user.nickname)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                    // 메인 액티비티로 이동
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish() // 로그인 화면 종료

                } else {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}