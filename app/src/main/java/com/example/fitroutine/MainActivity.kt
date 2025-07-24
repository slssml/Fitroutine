package com.example.fitroutine

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    true
                }
                R.id.fragment_search -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, SearchFragment()).commit()
                    true
                }
                R.id.fragment_favorite -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, FavoriteFragment()).commit()
                    true
                }
                R.id.fragment_settings -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, SettingsFragment()).commit()
                    true
                }
                else -> false
            }
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 홈 탭 클릭 시
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_video -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, VideoFragment())
                        .commit()
                    true
                }
                R.id.nav_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, MypageFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // 처음에 보여줄 프래그먼트
        bottomNavigation.selectedItemId = R.id.nav_home
    }
}