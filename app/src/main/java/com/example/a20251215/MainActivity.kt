package com.example.a20251215

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {


        // 스플래시는 super.onCreate()보다 먼저!
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            delay(3000)          //  3초 보여주기
            keepSplash = false   //  스플래시 내리기

            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)

            if (!isLoggedIn) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                return@launch
            }

            // ✅ 로그인 상태면 HomeFragment 붙이기
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, HomeFragment())
                    .commit()
            }
        }
    }
}
