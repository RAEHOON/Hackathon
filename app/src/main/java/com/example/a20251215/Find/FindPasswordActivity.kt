package com.example.a20251215.Find

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.a20251215.LoginActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.Sign.ResetPasswordActivity
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPasswordActivity : AppCompatActivity() {
    private var countDownTimer: CountDownTimer? = null

    private var serverAuthCode: String? = ""

    private lateinit var tv_timer: TextView
    private lateinit var btnConfirm: Button

    private lateinit var layoutLoading: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_password)

        val tilId = findViewById<TextInputLayout>(R.id.til_find_id)
        val etId = findViewById<EditText>(R.id.et_find_id)
        val etEmail = findViewById<EditText>(R.id.et_find_email)
        val btnRequest = findViewById<Button>(R.id.btn_request_auth)

        val tilEmail = findViewById<TextInputLayout>(R.id.til_find_email)
        val layoutAuth = findViewById<LinearLayout>(R.id.layout_auth_input)

        tv_timer = findViewById(R.id.tv_timer)

        val tilCode = findViewById<TextInputLayout>(R.id.til_auth_code)
        val etCode = findViewById<EditText>(R.id.et_auth_code)
        btnConfirm = findViewById(R.id.btn_auth_confirm)

        layoutLoading = findViewById(R.id.layout_loading)

        val backBtn = findViewById<ImageView>(R.id.back_btn)

        backBtn.setOnClickListener {
            startActivity(Intent(this@FindPasswordActivity, LoginActivity::class.java))
            finish()
        }

        etId.addTextChangedListener { tilId.error = null }
        etEmail.addTextChangedListener { tilEmail.error = null }
        etCode.addTextChangedListener { tilCode.error = null }

        btnRequest.setOnClickListener {
            val idInput = etId.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()

            if (idInput.isEmpty() || emailInput.isEmpty()) {
                tilId.error = "아이디를 입력해주세요."
                return@setOnClickListener
            }
            if (emailInput.isEmpty()) {
                tilEmail.error = "이메일을 입력해주세요."
                return@setOnClickListener
            }

            layoutLoading.visibility = View.VISIBLE
            btnRequest.isEnabled = false
            etId.isEnabled = false
            etEmail.isEnabled = false

            RetrofitClient.apiService.sendEmailCode(emailInput).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    layoutLoading.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(applicationContext, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show()

                        layoutAuth.visibility = View.VISIBLE

                        startTimer()
                    } else {
                        val msg = response.body()?.message ?: "메일 발송 실패"
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

                        btnRequest.isEnabled = true
                        etId.isEnabled = true
                        etEmail.isEnabled = true
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    layoutLoading.visibility = View.GONE
                    btnRequest.isEnabled = true
                    etId.isEnabled = true
                    etEmail.isEnabled = true

                    Toast.makeText(applicationContext, "서버 통신 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
        btnConfirm.setOnClickListener {
            val idInput = etId.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val codeInput = etCode.text.toString().trim()

            if (codeInput.isEmpty()) {
                tilCode.error = "인증번호를 입력해주세요."
                return@setOnClickListener
            }
            RetrofitClient.apiService.findPassword(idInput, emailInput, codeInput).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(applicationContext, "인증 성공!", Toast.LENGTH_SHORT).show()

                        countDownTimer?.cancel()

                        val intent = Intent(this@FindPasswordActivity, ResetPasswordActivity::class.java)
                        intent.putExtra("loginid", idInput)
                        intent.putExtra("email", emailInput)
                        startActivity(intent)

                        finish()
                    } else {
                        tilCode.error = "인증번호가 일치하지 않습니다."
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "서버 통신 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                tv_timer.setText(String.format("%02d:%02d", min, sec))
            }

            override fun onFinish() {
                tv_timer.setText("시간 초과")
                serverAuthCode = ""
                btnConfirm.setEnabled(false)
            }
        }.start()
    }
}