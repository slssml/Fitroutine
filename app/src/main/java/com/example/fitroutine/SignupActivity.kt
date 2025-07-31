package com.example.fitroutine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// 회원가입 화면 액티비티
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth        // Firebase 인증 객체
    private lateinit var db: FirebaseFirestore      // Firestore DB 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()          // FirebaseAuth 초기화
        db = FirebaseFirestore.getInstance()       // Firestore 초기화

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)
        val signupButton = findViewById<Button>(R.id.signupSubmitButton)

        // 회원가입 버튼 클릭 시
        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()

            // RadioGroup에서 선택된 RadioButton id 가져오기
            val selectedGenderId = genderRadioGroup.checkedRadioButtonId
            val selectedGenderRadioButton = if (selectedGenderId != -1) {
                findViewById<RadioButton>(selectedGenderId)
            } else null
            val gender = selectedGenderRadioButton?.text.toString()

            // 이메일, 비밀번호, 닉네입 미입력 시 메시지
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Authentication으로 가입 시도
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid   // 가입한 사용자의 고유 ID 가져오기
                        if (uid != null) {
                            val userInfo = hashMapOf(      // 사용자 추가 정보 맵 생성
                                "name" to name,
                                "age" to age,
                                "gender" to gender,
                                "email" to email
                            )
                            // Firestore에 사용자 정보 저장
                            db.collection("users").document(uid).set(userInfo)
                        }
                        Toast.makeText(this, "가입 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
        }
    }
}
