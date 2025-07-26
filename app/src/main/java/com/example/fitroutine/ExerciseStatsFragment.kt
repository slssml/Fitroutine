package com.example.fitroutine

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExerciseStatsFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private val exerciseList = mutableListOf<Exercise>()
    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_exercise_stats, container, false)

        calendarView = root.findViewById(R.id.calendarView)
        recyclerView = root.findViewById(R.id.recyclerView)
        emptyText = root.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ExerciseAdapter(exerciseList)
        recyclerView.adapter = adapter

        // 날짜 선택 시 운동 기록 불러오기
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("ExerciseStats", "Selected date: $selectedDate")
            loadExercises(selectedDate)
        }

        // 처음엔 오늘 날짜 운동 기록 보여주기
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

    // 운동 기록 불러오기
    private fun loadExercises(date: String) {
        if (currentUid == null) {
            Log.w("ExerciseStats", "User UID is null")
            showEmpty()
            return
        }

        val docRef = db.collection("users")
            .document(currentUid)
            .collection("dailyRecords")
            .document(date)

        docRef.get()
            .addOnSuccessListener { document ->
                Log.d("ExerciseStats", "Document exists: ${document.exists()}")
                exerciseList.clear()

                if (document.exists()) {
                    val exercises = document.get("exercises") as? List<Map<String, Any>>
                    Log.d("ExerciseStats", "Exercises: $exercises")
                    if (!exercises.isNullOrEmpty()) {
                        for (item in exercises) {
                            val name = item["exerciseName"] as? String ?: ""
                            val time = item["exerciseTime"] as? String ?: ""
                            exerciseList.add(Exercise(name, time))
                        }
                        adapter.notifyDataSetChanged()
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

    // 운동 기록 없을 때 처리
    private fun showEmpty() {
        exerciseList.clear()
        adapter.notifyDataSetChanged()
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
    }

    // 운동 데이터 클래스
    data class Exercise(val name: String, val time: String)

    // 어댑터
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
