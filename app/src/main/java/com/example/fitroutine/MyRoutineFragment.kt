package com.example.fitroutine

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val time: String = "",
    val days: List<String> = emptyList(),
    val routineName: String = ""   // name -> routineName으로 변경
)

class MyRoutineFragment : Fragment() {

    private lateinit var routineListView: ListView
    private lateinit var emptyTextView: TextView
    private lateinit var addRoutineButton: Button
    private val routineList = mutableListOf<Routine>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_routine, container, false)

        routineListView = view.findViewById(R.id.listViewRoutines)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        addRoutineButton = view.findViewById(R.id.AddRoutine)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        routineListView.adapter = adapter

        loadRoutinesFromFirebase()

        addRoutineButton.setOnClickListener {
            showAddRoutineDialog()
        }

        routineListView.setOnItemLongClickListener { _, _, position, _ ->
            val routine = routineList.getOrNull(position)
            if (routine != null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("루틴 삭제")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        deleteRoutine(routine)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            true
        }

        return view
    }

    // 루틴 정보를 문자열로 변환
    private fun formatRoutine(routine: Routine): String {
        return "[${routine.routineName}] ${routine.time} (${routine.days.joinToString(", ")})"
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
            updateListView()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 리스트뷰 갱신
    private fun updateListView() {
        adapter.clear()

        val visibleRoutines = routineList.filter { it.routineName.isNotEmpty() }

        val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        val sortedRoutines = visibleRoutines.sortedWith(compareBy { routine ->
            try {
                timeFormat.parse(routine.time)
            } catch (e: Exception) {
                null
            }
        })

        if (sortedRoutines.isNotEmpty()) {
            adapter.addAll(sortedRoutines.map { formatRoutine(it) })
            emptyTextView.visibility = View.GONE
            routineListView.visibility = View.VISIBLE
        } else {
            emptyTextView.visibility = View.VISIBLE
            routineListView.visibility = View.GONE
        }

        adapter.notifyDataSetChanged()
    }

    // 파이어베이스에 저장
    private fun saveRoutineToFirebase(routine: Routine) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .document(routine.id)
            .set(routine)
    }

    // 파이어베이스에서 삭제
    private fun deleteRoutine(routine: Routine) {
        routineList.remove(routine)
        updateListView()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .document(routine.id)
            .delete()
    }

    // 파이어베이스에서 루틴 불러오기
    private fun loadRoutinesFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .collection("routines")
            .get()
            .addOnSuccessListener { snapshot ->
                routineList.clear()
                for (doc in snapshot.documents) {
                    val routine = doc.toObject(Routine::class.java)
                    if (routine != null && routine.routineName.isNotEmpty()) {
                        routineList.add(routine)
                    }
                }
                updateListView()
            }
    }
}




