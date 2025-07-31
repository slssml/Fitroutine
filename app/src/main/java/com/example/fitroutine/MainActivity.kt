package com.example.fitroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitroutine.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 하단 내비게이션 설정
        setupBottomNavigation()

        // 앱 최초 실행 시 홈화면 표시
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, HomeFragment())
                .commit()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // 홈 프래그먼트
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, HomeFragment())
                        .commit()
                    true
                }
                // 영상 프래그먼트
                R.id.nav_video -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, VideoFragment())
                        .commit()
                    true
                }
                // 마이페이지 프래그먼트
                R.id.nav_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, MypageFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
