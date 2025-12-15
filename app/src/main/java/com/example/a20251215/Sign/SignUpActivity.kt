package com.example.a20251215

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.Sign.SignupEmailFragment

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, SignupEmailFragment())
                .commit()
        }
    }
}