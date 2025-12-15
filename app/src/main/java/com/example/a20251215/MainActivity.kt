package com.example.a20251215

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.MypageFragment.MypageFragment
import com.example.a20251215.Ranking.RankingFragment
import com.example.a20251215.Setting.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.home_container, HomeFragment())
                .commit()
            bottomNav.selectedItemId = R.id.nav_home

            bottomNav.setOnItemSelectedListener { item ->
                val fragment = when (item.itemId) {
                    R.id.nav_home -> HomeFragment()
                    R.id.nav_ranking -> RankingFragment()
                    R.id.nav_alarm -> AlarmFragment()
                    R.id.nav_mypage -> MypageFragment()
                    R.id.nav_settings -> SettingsFragment()
                    else -> null
                }

                fragment?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_container, it)
                        .commit()
                    true
                } ?: false
            }
        }
    }
}