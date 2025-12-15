package com.example.a20251215.Sign

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupNicknameFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_nickname, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val receivedName = arguments?.getString("username") ?: ""
        val receivedId = arguments?.getString("loginid") ?: ""

        val etNickname = view.findViewById<EditText>(R.id.et_nickname)
        val nextBtn = view.findViewById<Button>(R.id.btn_nickname_next)
        val receivedEmail = arguments?.getString("email") ?: ""

        nextBtn.setOnClickListener {
            val inputNickname = etNickname.text.toString().trim()

            if (inputNickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNicknameOnServer(inputNickname, receivedName, receivedId, receivedEmail)
        }
    }

    private fun checkNicknameOnServer(nickname: String, name: String, id: String, email: String) {
        RetrofitClient.apiService.checkNicknameDuplicate(nickname).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()

                    goToPasswordFragment(name, id, nickname, email)
                } else {
                    val msg = response.body()?.message ?: "이미 사용 중인 닉네임입니다."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("NicknameCheck", t.message.toString())
                Toast.makeText(context, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun goToPasswordFragment(name: String, id: String, nickname: String, email: String) {
        val fragment = SignupPasswordFragment()
        val bundle = Bundle()

        bundle.putString("username", name)
        bundle.putString("loginid", id)
        bundle.putString("nickname", nickname)
        bundle.putString("email", email)

        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}