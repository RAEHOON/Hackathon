package com.example.a20251215.Sign

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.ValidationUtils
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.matches

class SignupEmailFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var tvTimer: TextView
    private lateinit var btnConfirm: Button
    private lateinit var layoutLoading: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilEmail = view.findViewById<TextInputLayout>(R.id.til_email)
        val etEmail = view.findViewById<EditText>(R.id.et_find_email)
        val btnRequest = view.findViewById<Button>(R.id.btn_find_id_request)

        val layoutAuth = view.findViewById<LinearLayout>(R.id.layout_find_id_auth)

        val tilCode = view.findViewById<TextInputLayout>(R.id.til_auth_code)
        val etCode = view.findViewById<EditText>(R.id.et_find_id_code)

        tvTimer = view.findViewById(R.id.tv_find_id_timer)
        btnConfirm = view.findViewById(R.id.btn_find_id_confirm)
        layoutLoading = view.findViewById(R.id.layout_loading)

        val confirmBtn = view.findViewById<Button>(R.id.btn_find_id_confirm)

        etEmail.addTextChangedListener { tilEmail.error = null }
        etCode.addTextChangedListener { tilCode.error = null }

        btnRequest.setOnClickListener {
            val inputEmail = etEmail.text.toString()

            if (!ValidationUtils.EMAIL_PATTERN.matcher(inputEmail).matches()) {
                tilEmail.error = "올바른 이메일 형식이 아닙니다."
                return@setOnClickListener
            }

            layoutLoading.visibility = View.VISIBLE
            btnRequest.isEnabled = false
            etEmail.isEnabled = false


            RetrofitClient.apiService.sendEmailCode(inputEmail).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    layoutLoading.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()

                        layoutAuth.visibility = View.VISIBLE

                        startTimer()

                    } else {
                        Toast.makeText(context, "메일 발송 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                        btnRequest.isEnabled = true
                        etEmail.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }


            })

        }

        confirmBtn.setOnClickListener {
            val inputEmail = etEmail.text.toString()
            val inputCode = etCode.text.toString()

            if (inputCode.isEmpty()) {
                Toast.makeText(context, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkAuthCode(inputEmail, inputCode)
        }
    }

    private fun checkAuthCode(email: String, code: String) {
        RetrofitClient.apiService.checkEmailCode(email, code).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "인증에 성공했습니다.", Toast.LENGTH_SHORT).show()

                    countDownTimer?.cancel()

                    val fragment = SignupUserIdFragment()
                    val bundle = Bundle()
                    bundle.putString("email", email)
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(context, "인증 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "서버 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startTimer() {
        countDownTimer?.cancel()

        // 3분(180초) 타이머
        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", min, sec)
            }

            override fun onFinish() {
                tvTimer.text = "시간 초과"
                btnConfirm.isEnabled = false // 시간 초과 시 확인 버튼 비활성화
            }
        }.start()
    }

    // 프래그먼트가 사라질 때 타이머 종료 (메모리 누수 방지)
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}

