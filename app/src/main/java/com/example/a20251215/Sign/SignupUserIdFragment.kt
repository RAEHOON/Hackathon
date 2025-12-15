package com.example.a20251215.Sign

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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

class SignupUserIdFragment: Fragment() {
    private lateinit var usernameEt: EditText
    private lateinit var userIdEt: EditText

    private var receivedEmail: String = ""

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilUserId: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receivedEmail = arguments?.getString("email") ?: ""

        tilUsername = view.findViewById(R.id.til_username)
        usernameEt = view.findViewById(R.id.et_username)
        val checknameBtn = view.findViewById<Button>(R.id.btn_check_username)

        tilUserId = view.findViewById(R.id.til_user_id)
        val layoutIdAuth = view.findViewById<LinearLayout>(R.id.layout_find_id_auth)
        userIdEt = view.findViewById(R.id.et_user_id)
        val checkIdBtn = view.findViewById<Button>(R.id.btn_user_id)

        usernameEt.addTextChangedListener {
            tilUsername.error = null
        }

        userIdEt.addTextChangedListener {
            tilUserId.error = null
        }

        checknameBtn.setOnClickListener {
            val name = usernameEt.text.toString().trim()

            if (name.isEmpty()) {
                tilUsername.error = "이름을 입력해주세요."
                return@setOnClickListener
            }

            Toast.makeText(context, "이름이 확인되었습니다.", Toast.LENGTH_SHORT).show()

            usernameEt.isEnabled = false
            checknameBtn.isEnabled = false
            checknameBtn.visibility = View.GONE

            layoutIdAuth.visibility = View.VISIBLE
        }

        checkIdBtn.setOnClickListener {
            val inputId = userIdEt.text.toString().trim()

            if (!ValidationUtils.ID_PATTERN.matcher(inputId).matches()) {
                tilUserId.error = "아이디는 영문+숫자 4자 이상이어야 합니다."
                return@setOnClickListener
            }

            checkIdOnServer(inputId)
        }
    }

    private fun checkIdOnServer(id: String) {
        RetrofitClient.apiService.checkIdDuplicate(id).enqueue(object : Callback<ApiResponse> {

            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()

                    tilUserId.error = null

                    val currentName = usernameEt.text.toString().trim()
                    goToPasswordFragment(currentName, id, receivedEmail)
                } else {
                    val msg = response.body()?.message ?: "이미 사용 중인 아이디입니다."
                    tilUserId.error = msg
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun goToPasswordFragment(name: String, id: String, email: String) {
        val fragment = SignupNicknameFragment()

        val bundle = Bundle()
        bundle.putString("username", name)
        bundle.putString("loginid", id)
        bundle.putString("email", email)
        fragment.arguments = bundle

        // 화면 전환
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}