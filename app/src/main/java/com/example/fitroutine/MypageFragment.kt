package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitroutine.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    // Firebase 인증과 Firestore 인스턴스 초기화
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 프래그먼트 뷰 생성 및 바인딩 설정
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰 만들어진 후 초기화
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firestore에서 닉네임 불러오기
        loadNicknameFromFirebase()

        // 버튼 클릭 연결
        binding.WeightStats.setOnClickListener { navigateToWeightStats() }
        binding.ExerciseStats.setOnClickListener { navigateToExerciseStats() }
        binding.MyRoutine.setOnClickListener { navigateToMyRoutine() }
        binding.Favorites.setOnClickListener { navigateToFavorites() }
    }

    // Firestore에서 사용자 닉네임 가져와 텍스트뷰에 세팅
    private fun loadNicknameFromFirebase() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: ""
                binding.name.text = "$name 님 💪"
            }
    }

    // 체중 통계 화면으로 전환
    private fun navigateToWeightStats() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frame, WeightStatsFragment())
            .addToBackStack(null)
            .commit()
    }

    // 운동 통계 화면으로 전환
    private fun navigateToExerciseStats() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frame, ExerciseStatsFragment())
            .addToBackStack(null)
            .commit()
    }

    // 루틴 관리 화면으로 전환
    private fun navigateToMyRoutine() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frame, MyRoutineFragment())
            .addToBackStack(null)
            .commit()
    }

    // 즐겨찾기 화면 전환
    private fun navigateToFavorites() {

    }

    // 뷰가 화면에서 제거될 때 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

