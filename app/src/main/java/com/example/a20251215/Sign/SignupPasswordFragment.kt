package com.example.a20251215.Sign

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.ValidationUtils
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.matches

class SignupPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val receivedName = arguments?.getString("username") ?: ""
        val receivedId = arguments?.getString("loginid") ?: ""
        val receivedNickname = arguments?.getString("nickname") ?: ""
        val receivedEmail = arguments?.getString("email") ?: ""

        val tilPassword = view.findViewById<TextInputLayout>(R.id.til_password)
        val etPassword = view.findViewById<EditText>(R.id.et_password)

        val tilPasswordAgain = view.findViewById<TextInputLayout>(R.id.til_password_again)
        val etPasswordAgain = view.findViewById<EditText>(R.id.et_password_again)

        val finishBtn = view.findViewById<Button>(R.id.btn_signup_finish)

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

            requestSignup(receivedName, receivedNickname, receivedId, pw, receivedEmail)
        }
    }
    private fun requestSignup(name: String, nickname: String, id: String, pw: String, email: String) {
        RetrofitClient.apiService.signup(
            username = name,
            nickname = nickname,
            loginid = id,
            email = email,
            password = pw
        ).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_LONG).show()
                    activity?.finish()
                } else {
                    val msg = response.body()?.message ?: "회원가입에 실패했습니다."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Log.e("SignupError", t.message.toString())
                Toast.makeText(context, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}