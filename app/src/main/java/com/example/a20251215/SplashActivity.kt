//package com.example.a20251215
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//class SplashActivity : AppCompatActivity() {
//
//    private var keepSplash = true
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        val splash = installSplashScreen()
//        splash.setKeepOnScreenCondition { keepSplash }
//
//        super.onCreate(savedInstanceState)
//
//        lifecycleScope.launch {
//            delay(3000)
//            keepSplash = false
//
//            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
//            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
//
//            if (isLoggedIn) {
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
//                    // 뒤로가기 했을 때 스플래시/로그인으로 안 돌아가게
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                })
//            } else {
//                startActivity(Intent(this@SplashActivity, LoginActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                })
//            }
//        }
//    }
//}
