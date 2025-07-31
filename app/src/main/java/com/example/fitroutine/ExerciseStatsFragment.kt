package com.example.fitroutine

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// 운동 기록을 날짜 별로 보여주는 프래그먼트
class ExerciseStatsFragment : Fragment() {

    private lateinit var calendarView: CalendarView  // 날짜 선택 달력
    private lateinit var recyclerView: RecyclerView  // 운동 기록 리스트 보여줄 리사이클뷰
    private lateinit var emptyText: TextView         // 운동 기록 없을 때 보여줄 텍스트

    private val db = FirebaseFirestore.getInstance() // Firestore 인스턴스
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid  // 현재 로그인한 사용자 UID

    private val exerciseList = mutableListOf<Exercise>()   // 현재 선택한 날짜의 운동 기록 리스트
    private lateinit var adapter: ExerciseAdapter          // 리사이클뷰 어댑터

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_exercise_stats, container, false)

        // 뷰 초기화
        calendarView = root.findViewById(R.id.calendarView)
        recyclerView = root.findViewById(R.id.recyclerView)
        emptyText = root.findViewById(R.id.emptyText)

        // 리사이클뷰 레이아웃 매니저 설정
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 어댑터 생성 및 리사이클뷰에 연결
        adapter = ExerciseAdapter(exerciseList)
        recyclerView.adapter = adapter

        // 달력에서 날짜 선택 시 운동 기록 불러오기
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("ExerciseStats", "Selected date: $selectedDate")
            loadExercises(selectedDate)
        }

        // 앱 실행 시 기본으로 오늘 날짜 운동 기록 보여주기
        val today = java.util.Calendar.getInstance()
        val todayDate = String.format(
            "%04d-%02d-%02d",
            today.get(java.util.Calendar.YEAR),
            today.get(java.util.Calendar.MONTH) + 1,
            today.get(java.util.Calendar.DAY_OF_MONTH)
        )
        loadExercises(todayDate)

        return root
    }

    // 특정 날짜 운동 기록을 Firestore에서 불러와 리스트에 업데이트
    private fun loadExercises(date: String) {
        // 현재 로그인한 사용자 UID가 없으면 빈 화면 처리
        if (currentUid == null) {
            Log.w("ExerciseStats", "User UID is null")
            showEmpty()
            return
        }

        // Firestore 문서 경로 지정 (users/{uid}/dailyRecords/{date})
        val docRef = db.collection("users")
            .document(currentUid)
            .collection("dailyRecords")
            .document(date)

        docRef.get()
            .addOnSuccessListener { document ->
                Log.d("ExerciseStats", "Document exists: ${document.exists()}")
                exerciseList.clear()

                if (document.exists()) {
                    // exercises 필드에서 리스트 추출
                    val exercises = document.get("exercises") as? List<Map<String, Any>>
                    Log.d("ExerciseStats", "Exercises: $exercises")
                    if (!exercises.isNullOrEmpty()) {
                        // 각 운동 데이터를 Exercise 객체로 변환해 리스트에 추가
                        for (item in exercises) {
                            val name = item["exerciseName"] as? String ?: ""
                            val time = item["exerciseTime"] as? String ?: ""
                            exerciseList.add(Exercise(name, time))
                        }
                        adapter.notifyDataSetChanged()  // 리스트 갱신
                        recyclerView.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE
                    } else {
                        showEmpty()
                    }
                } else {
                    showEmpty()
                }
            }
            .addOnFailureListener {
                showEmpty()
            }
    }

    // 운동 기록 없을 때 처리 리사이클뷰 숨기고 빈 텍스트 보여주는 함수
    private fun showEmpty() {
        exerciseList.clear()
        adapter.notifyDataSetChanged()
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
    }

    // 운동 데이터 클래스
    data class Exercise(val name: String, val time: String)

    // 리사이클뷰 어댑터
    class ExerciseAdapter(private val items: List<Exercise>) :
        RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.exerciseName)
            val timeText: TextView = view.findViewById(R.id.exerciseTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercise, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.nameText.text = "운동 이름 : ${item.name}"
            holder.timeText.text = "운동 시간 : ${item.time}분"
        }

        override fun getItemCount() = items.size
    }
}
