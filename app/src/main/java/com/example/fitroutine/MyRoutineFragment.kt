package com.example.fitroutine

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class Routine(
    val id: String = UUID.randomUUID().toString(),       // 고유 ID 자동 생성
    val time: String = "",                                // 시간
    val days: List<String> = emptyList(),                 // 선택 요일
    val routineName: String = ""                           // 루틴 이름
)

class MyRoutineFragment : Fragment() {

    private lateinit var routineRecyclerView: RecyclerView
    private lateinit var addRoutineButton: Button
    private lateinit var emptyTextView: TextView

    private val routineList = mutableListOf<Routine>()    // 루틴 리스트
    private lateinit var adapter: MyRoutineAdapter        // 어댑터

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_routine, container, false)

        routineRecyclerView = view.findViewById(R.id.recyclerViewRoutines)
        addRoutineButton = view.findViewById(R.id.AddRoutine)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        adapter = MyRoutineAdapter(
            routineList,
            showCheckbox = false,
            onLongClick = { routine ->
                showDeleteDialog(routine)},
            onItemClick = { routine ->
                loadVideosForRoutine(routine.id)
            }
        )

        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = adapter

        addRoutineButton.setOnClickListener {
            showAddRoutineDialog()
        }

        loadRoutinesFromFirebase()
        return view
    }

    // 루틴 추가 다이얼로그 띄우기
    private fun showAddRoutineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_routine, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val editName = dialogView.findViewById<EditText>(R.id.editRoutineName)

        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.cbSun),
            dialogView.findViewById<CheckBox>(R.id.cbMon),
            dialogView.findViewById<CheckBox>(R.id.cbTue),
            dialogView.findViewById<CheckBox>(R.id.cbWed),
            dialogView.findViewById<CheckBox>(R.id.cbThu),
            dialogView.findViewById<CheckBox>(R.id.cbFri),
            dialogView.findViewById<CheckBox>(R.id.cbSat)
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val hour: Int
            val minute: Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour
                minute = timePicker.currentMinute
            }

            val selectedDays = checkBoxes.filter { it.isChecked }.map { it.text.toString() }
            val routineName = editName.text.toString().trim()

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val time = SimpleDateFormat("a h:mm", Locale.getDefault()).format(cal.time)

            val routine = Routine(time = time, days = selectedDays, routineName = routineName)
            routineList.add(routine)
            saveRoutineToFirebase(routine)

            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Firebase에 루틴 저장
    private fun saveRoutineToFirebase(routine: Routine) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .document(routine.id)
            .set(routine)
    }

    // Firebase에서 루틴 불러오기
    private fun loadRoutinesFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .get()
            .addOnSuccessListener { result ->
                routineList.clear()
                for (document in result) {
                    val routine = document.toObject(Routine::class.java)
                    routineList.add(routine)
                }
                adapter.notifyDataSetChanged()

                emptyTextView.visibility = if (routineList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    // 루틴 삭제 확인 다이얼로그 띄우기
    private fun showDeleteDialog(routine: Routine) {
        AlertDialog.Builder(requireContext())
            .setTitle("루틴 삭제")
            .setMessage("루틴 \"${routine.routineName}\" 을(를) 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteRoutine(routine)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // Firebase에서 루틴 삭제 처리
    private fun deleteRoutine(routine: Routine) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .document(routine.id)
            .delete()
            .addOnSuccessListener {
                routineList.remove(routine)
                adapter.notifyDataSetChanged()
                emptyTextView.visibility = if (routineList.isEmpty()) View.VISIBLE else View.GONE
                Toast.makeText(requireContext(), "루틴이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadVideosForRoutine(routineId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .document(routineId)
            .collection("videos")
            .get()
            .addOnSuccessListener { snapshot ->
                val videoList = snapshot.map { doc ->
                    VideoItem(
                        id = 0,
                        title = doc.getString("title") ?: "제목 없음",
                        youtubeUrl = doc.getString("youtubeUrl") ?: "",
                        category = ""
                    )
                }
                if (videoList.isEmpty()) {
                    Toast.makeText(requireContext(), "루틴에 등록된 영상이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val fragment = RoutineVideoListFragment.newInstance(ArrayList(videoList))
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "영상 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

}


