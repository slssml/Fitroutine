package com.example.fitroutine

import android.os.Bundle
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


@Suppress("UNREACHABLE_CODE")
class HomeFragment : Fragment() {

    private lateinit var greetingText: TextView
    private lateinit var weightInput: EditText
    private lateinit var addWeightButton: Button
    private lateinit var exerciseNameEditText: EditText
    private lateinit var exerciseTimeEditText: EditText
    private lateinit var addExerciseButton: Button
    private lateinit var routineRecyclerView: RecyclerView
    private lateinit var showMoreBtn: Button

    private val allRoutines = mutableListOf("마이루틴 1", "마이루틴 2", "마이루틴 3", "마이루틴 4", "마이루틴 5")
    private var showingAll = false
    private lateinit var routineAdapter: MyRoutineAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        updateRoutineList()

        //더보기 버튼 눌렀을 때
        showMoreBtn.setOnClickListener {
            showingAll = !showingAll
            updateRoutineList()
            showMoreBtn.text = if (showingAll) "접기" else "더보기"
        }


        //체중 저장 버튼 눌렀을 때
        addWeightButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val weight = weightInput.text.toString().toFloatOrNull()

            if (weight == null) {
                Toast.makeText(context, "체중을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = mapOf(
                "weight" to weightInput.text.toString().toFloat()
            )

            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("dailyRecords").document(today)
                .set(data, SetOptions.merge())  // weight 필드만 병합 저장
                .addOnSuccessListener {
                    Toast.makeText(context, "체중 저장 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        //운동 저장 버튼 눌렀을 때
        addExerciseButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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

            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("dailyRecords").document(today)
                .update("exercises", FieldValue.arrayUnion(data))
                .addOnSuccessListener {
                    Toast.makeText(context, "운동 기록 추가 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // 만약 문서가 존재하지 않아 update 실패한 경우, 새로 만들기
                    val newData = mapOf("exercises" to listOf(data))
                    FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun updateRoutineList() {
        val displayList = if (showingAll) allRoutines else allRoutines.take(3)
        routineAdapter.updateList(displayList)
        showMoreBtn.visibility = if (allRoutines.size > 3) View.VISIBLE else View.GONE
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}