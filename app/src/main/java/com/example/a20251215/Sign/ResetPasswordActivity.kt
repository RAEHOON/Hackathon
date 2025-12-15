package com.example.a20251215.Sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.a20251215.LoginActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.ValidationUtils
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java
import kotlin.text.isEmpty
import kotlin.text.trim
import kotlin.toString

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val receivedId = intent.getStringExtra("loginid") ?: ""
        val receivedEmail = intent.getStringExtra("email") ?: ""

        val tilPassword = findViewById<TextInputLayout>(R.id.til_reset_password)
        val etPassword = findViewById<EditText>(R.id.et_reset_password)

        val tilPasswordAgain = findViewById<TextInputLayout>(R.id.til_reset_password_again)
        val etPasswordAgain = findViewById<EditText>(R.id.et_reset_password_again)

        val finishBtn = findViewById<Button>(R.id.btn_reset_finish)
        val backBtn = findViewById<ImageView>(R.id.back_btn)

        backBtn.setOnClickListener { finish() }

        etPassword.addTextChangedListener {
            tilPassword.error = null
        }

        etPasswordAgain.addTextChangedListener {
            tilPasswordAgain.error = null
        }

        finishBtn.setOnClickListener {
            val pw = etPassword.text.toString().trim()
            val pwAgain = etPasswordAgain.text.toString().trim()

            if (!ValidationUtils.PW_PATTERN.matcher(pw).matches()) {
                tilPassword.error = "비밀번호는 영문+숫자+특수문자 포함 8~20자여야 합니다."
                return@setOnClickListener
            }

            if (pw != pwAgain) {
                tilPasswordAgain.error = "비밀번호가 일치하지 않습니다."
                return@setOnClickListener
            }

            tilPassword.error = null
            tilPasswordAgain.error = null

            requestResetPassword(receivedId, receivedEmail, pw)
        }
    }

    private fun requestResetPassword(id: String, email: String, newPw: String) {
        if (id.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.resetPasswordByEmail(
            loginid = id,
            email = email,
            newPassword = newPw
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(applicationContext, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val msg = response.body()?.message ?: "비밀번호 변경 실패"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("ResetPwError", t.message.toString())
                Toast.makeText(applicationContext, "서버 통신 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}