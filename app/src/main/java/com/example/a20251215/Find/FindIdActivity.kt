package com.example.a20251215.Find

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.LoginActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        val editText_name = findViewById<EditText>(R.id.find_username)
        val editText_email = findViewById<EditText>(R.id.find_email)
        val button_find = findViewById<Button>(R.id.find_id_btn)
        val button_back = findViewById<ImageView>(R.id.back_btn)

        button_back.setOnClickListener {
            finish()
        }

        button_find.setOnClickListener {
            val name = editText_name.text.toString().trim()
            val email = editText_email.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "이름과 이메일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findUserId(name, email)
        }

    }

    private fun findUserId(name: String, email: String) {
        RetrofitClient.apiService.findId(name, email).enqueue(object : Callback<FindIdResponse> {
            override fun onResponse(call: Call<FindIdResponse>, response: Response<FindIdResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val foundId = response.body()?.loginid

                    AlertDialog.Builder(this@FindIdActivity)
                        .setTitle("아이디 찾기 성공")
                        .setMessage("회원님의 아이디는 [ $foundId ] 입니다.")
                        .setPositiveButton("로그인 하기") { _, _ ->
                            startActivity(Intent(this@FindIdActivity, LoginActivity::class.java))
                            finish()

                        }
                        .show()
                } else {
                    val msg = response.body()?.message ?: "일치하는 정보가 없습니다."
                    Toast.makeText(this@FindIdActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
                Log.e("FindIdError", t.message.toString())
                Toast.makeText(this@FindIdActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}