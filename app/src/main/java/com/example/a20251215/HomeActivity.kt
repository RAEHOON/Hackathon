package com.example.a20251215

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity  : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.home_container, HomeFragment())
                .commit()
        }


    }
}