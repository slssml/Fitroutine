package com.example.fitroutine

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var greetingText: TextView
    private lateinit var weightInput: EditText
    private lateinit var addWeightButton: Button
    private lateinit var exerciseNameEditText: EditText
    private lateinit var exerciseTimeEditText: EditText
    private lateinit var addExerciseButton: Button
    private lateinit var routineRecyclerView: RecyclerView
    private lateinit var showMoreBtn: Button

    private val routineList = mutableListOf<String>()
    private var showingAll = false
    private lateinit var routineAdapter: MyRoutineAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val today: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View 초기화
        greetingText = view.findViewById(R.id.greetingText)
        weightInput = view.findViewById(R.id.weightInput)
        addWeightButton = view.findViewById(R.id.addWeightButton)
        exerciseNameEditText = view.findViewById(R.id.exerciseNameEditText)
        exerciseTimeEditText = view.findViewById(R.id.exerciseTimeEditText)
        addExerciseButton = view.findViewById(R.id.addExerciseButton)
        routineRecyclerView = view.findViewById(R.id.routineRecyclerView)
        showMoreBtn = view.findViewById(R.id.showMoreBtn)

        routineAdapter = MyRoutineAdapter(emptyList())
        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = routineAdapter

        updateRoutineList() // 루틴 업데이트 호출

        // 사용자 이름 불러와서 인사말 출력
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    greetingText.text = "${name}님, 오늘 하루도 파이팅!"
                }
        }

        // 더보기 버튼 클릭 처리
        showMoreBtn.setOnClickListener {
            showingAll = !showingAll
            updateRoutineList()
            showMoreBtn.text = if (showingAll) "접기" else "더보기"
        }

        // 체중 저장 버튼
        addWeightButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val weight = weightInput.text.toString().toFloatOrNull()

            if (weight == null) {
                Toast.makeText(context, "체중을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = mapOf("weight" to weight)

            db.collection("users").document(uid)
                .collection("dailyRecords").document(today)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, "체중 저장 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 운동 저장 버튼
        addExerciseButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val name = exerciseNameEditText.text.toString()
            val time = exerciseTimeEditText.text.toString()

            if (name.isEmpty() || time.isEmpty()) {
                Toast.makeText(context, "운동 이름과 시간을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "exerciseName" to name,
                "exerciseTime" to time
            )

            db.collection("users").document(uid)
                .collection("dailyRecords").document(today)
                .update("exercises", FieldValue.arrayUnion(data))
                .addOnSuccessListener {
                    Toast.makeText(context, "운동 기록 추가 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    val newData = mapOf("exercises" to listOf(data))
                    db.collection("users").document(uid)
                        .collection("dailyRecords").document(today)
                        .set(newData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(context, "운동 기록 새로 저장됨!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    // Firestore에서 루틴 불러와서 화면에 표시
    private fun updateRoutineList() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("routines")
            .get()
            .addOnSuccessListener { result ->
                routineList.clear()
                for (doc in result) {
                    val routineName = doc.getString("routineName") ?: ""
                    val time = doc.getString("time") ?: ""
                    val days = doc.get("days") as? List<String> ?: emptyList()
                    routineList.add("$routineName / $time / ${days.joinToString(", ")}")
                }
                // 로그로 출력해서 실제 리스트 확인
                Log.d("HomeFragment", "Loaded routines: $routineList")

                val displayList = if (showingAll) routineList else routineList.take(3)
                routineAdapter.updateList(displayList)

                showMoreBtn.visibility = if (routineList.size > 3) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Log.e("HomeFragment", "Failed to load routines", it)
            }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = HomeFragment()
    }
}



