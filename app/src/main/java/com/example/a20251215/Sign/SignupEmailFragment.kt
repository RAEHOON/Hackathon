package com.example.a20251215.Sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.ValidationUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupEmailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.et_find_email)
        val btnRequest = view.findViewById<Button>(R.id.btn_find_id_request)

        val layoutAuth = view.findViewById<LinearLayout>(R.id.layout_find_id_auth)
        val etCode = view.findViewById<EditText>(R.id.et_find_id_code)
        val confirmBtn = view.findViewById<Button>(R.id.btn_find_id_confirm)


        btnRequest.setOnClickListener {
            val inputEmail = etEmail.text.toString()

            if (!ValidationUtils.EMAIL_PATTERN.matcher(inputEmail).matches()) {
                Toast.makeText(context, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RetrofitClient.apiService.sendEmailCode(inputEmail).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(context, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()

                        layoutAuth.visibility = View.VISIBLE
                        etEmail.isEnabled = false
                        btnRequest.isEnabled = false
                    } else {
                        Toast.makeText(context, "메일 발송 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
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

                    // 수정된 부분: 다음 프래그먼트 생성 및 데이터 전달
                    val fragment = SignupUserIdFragment()
                    val bundle = Bundle()
                    bundle.putString("email", email) // ★ 여기서 이메일을 담아줍니다.
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
}