package com.example.fitroutine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// 로그인 화면 담당 액티비티
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth  // Firebase 인증 객체

    // 이메일, 비밀번호 입력 필드 및 로그인 버튼
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // 로그인 화면 연결

        auth = FirebaseAuth.getInstance()       // Firebase 인증 초기화

        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.loginButton)

        // 로그인 버튼 클릭 시 동작
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // 이메일 미입력 시 경고 메시지
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호 미입력 시 경고 메시지
            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase를 통해 이메일/비밀번호 로그인 시도
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공 시
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // 로그인 후 메인 화면으로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // 현재 화면 종료
                    } else {
                        // 로그인 실패 시 에러 메시지
                        Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}



