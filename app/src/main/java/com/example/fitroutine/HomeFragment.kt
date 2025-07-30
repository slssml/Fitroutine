package com.example.fitroutine

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    // UI 요소 선언
    private lateinit var greetingText: TextView
    private lateinit var routineRecyclerView: RecyclerView
    private lateinit var addTdRoutineBtn: Button
    private lateinit var showMoreBtn: Button
    private lateinit var achievementRateTextView: TextView
    private lateinit var weightInput: EditText
    private lateinit var addWeightButton: Button
    private lateinit var exerciseNameEditText: EditText
    private lateinit var exerciseTimeEditText: EditText
    private lateinit var addExerciseButton: Button

    // Firebase 관련 변수 초기화
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 날짜 포맷 : yyyy-MM-dd
    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // 루틴 리스트 및 체크 상태
    private val allRoutineList = mutableListOf<Routine>()
    private val todayRoutineList = mutableListOf<Routine>()
    private val checkedStatus = mutableMapOf<String, Boolean>()

    // 어댑터
    private lateinit var routineAdapter: MyRoutineAdapter

    // 더보기 버튼 눌러서 전체 루틴을 보여줄 지 여부 플래그
    private var showingAll = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // UI 초기화
        greetingText = view.findViewById(R.id.greetingText)
        routineRecyclerView = view.findViewById(R.id.routineRecyclerView)
        addTdRoutineBtn = view.findViewById(R.id.addTdRoutineBtn)
        showMoreBtn = view.findViewById(R.id.showMoreBtn)
        achievementRateTextView = view.findViewById(R.id.achievementRateTextView)
        weightInput = view.findViewById(R.id.weightInput)
        addWeightButton = view.findViewById(R.id.addWeightButton)
        exerciseNameEditText = view.findViewById(R.id.exerciseNameEditText)
        exerciseTimeEditText = view.findViewById(R.id.exerciseTimeEditText)
        addExerciseButton = view.findViewById(R.id.addExerciseButton)

        // 어댑터 설정 : 오늘 선택된 루틴 리스트, 체크 상태, 체크 상태 변경 콜백, 삭제 콜백 전달
        routineAdapter = MyRoutineAdapter(
            todayRoutineList,
            checkedStatus,
            ::saveRoutineStatus,
            showCheckbox = true,
            onLongClick = { routine -> showDeleteDialog(routine) }
        )


        // RecyclerView에 레이아웃 매니저와 어댑터 설정
        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = routineAdapter

        // Firebase에서 사용자 이름 불러와 인사말에 표시
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    greetingText.text = "${name ?: "사용자"}님, 오늘 하루도 파이팅!"
                }
        }

        // 버튼 이벤트
        addTdRoutineBtn.setOnClickListener { showSelectRoutineDialog() } // 루틴 추가 다이얼로그 열기
        showMoreBtn.setOnClickListener {
            // 더보기/접기 토글, UI 갱신, 버튼 텍스트 변경
            showingAll = !showingAll
            updateRoutineAdapterAndUI()
            showMoreBtn.text = if (showingAll) "접기" else "더보기"
        }
        addWeightButton.setOnClickListener { saveWeightToFirestore(weightInput.text.toString().toDoubleOrNull() ?: return@setOnClickListener) }
        addExerciseButton.setOnClickListener { saveExerciseToFirestore(exerciseNameEditText.text.toString(), exerciseTimeEditText.text.toString()) }

        // Firebase에서 전체 루틴 -> 오늘 루틴 -> 오늘 체크 상태 순서로 불러오기
        loadAllRoutines {
            loadTodayRoutines {
                loadRoutineStatus()
            }
        }
    }

    // Firebase에서 전체 루틴 목록 불러와 allRoutineList에 저장
    private fun loadAllRoutines(onLoaded: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("routines")
            .get()
            .addOnSuccessListener { result ->
                allRoutineList.clear()
                result.documents.mapNotNullTo(allRoutineList) { it.toObject(Routine::class.java) }
                onLoaded()
            }
    }

    // Firebase에서 오늘 선택된 루틴 id 리스트를 불러와 todayRoutineList에 해당 루틴 객체들로 채움
    private fun loadTodayRoutines(onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid).collection("today_routines").document(todayDate)

        docRef.get().addOnSuccessListener { doc ->
            val selectedIds = doc.get("routineIds") as? List<String> ?: emptyList()
            todayRoutineList.clear()
            todayRoutineList.addAll(allRoutineList.filter { it.id in selectedIds })
            onComplete()
        }
    }

    // Firebase에서 오늘 체크 상태를 불러와 checkStatus에 저장 후 UI 갱신
    private fun loadRoutineStatus() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("routine_status").document(todayDate)
            .get()
            .addOnSuccessListener { doc ->
                checkedStatus.clear()
                (doc.data ?: emptyMap()).forEach { (key, value) ->
                    checkedStatus[key] = value as? Boolean ?: false
                }
                updateRoutineAdapterAndUI()
            }
    }

    // 체크박스 상태가 변경되면 호출되는 콜백
    // 체크 상태 저장 후 목표 달성률 갱신
    private fun saveRoutineStatus(routineId: String, isChecked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        checkedStatus[routineId] = isChecked
        db.collection("users").document(uid)
            .collection("routine_status").document(todayDate)
            .set(checkedStatus)

        updateAchievementRate(todayRoutineList)
    }

    // 루틴 선택 다이얼로그 표시
    private fun showSelectRoutineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_routine_selector, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.routineRecyclerView)
        val btnAddSelected = dialogView.findViewById<Button>(R.id.btnAddSelected)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val selectedMap = mutableMapOf<String, Boolean>()
        val dialogAdapter = RoutineSelectAdapter(allRoutineList, selectedMap)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = dialogAdapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("루틴 선택")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnAddSelected.setOnClickListener {
            // 체크된 루틴 ID들만 추출
            val selectedIds = selectedMap.filterValues { it }.keys.toList()

            // 현재 todayRoutineList에 없는 루틴만 추가
            val newlySelectedRoutines = allRoutineList.filter { it.id in selectedIds && it !in todayRoutineList }
            todayRoutineList.addAll(newlySelectedRoutines)

            // 선택된 전체 루틴 ID 저장을 위해 현재 리스트에서 모두 ID 추출하여 Firestore에 저장
            val updatedSelectedIds = todayRoutineList.map { it.id }
            saveSelectedRoutineIdsToFirestore(updatedSelectedIds)

            // UI 갱신 후 다이얼로그 닫기
            updateRoutineAdapterAndUI()
            dialog.dismiss()
        }


        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Firestore에 오늘 선택된 루틴 ID 리스트 저장
    private fun saveSelectedRoutineIdsToFirestore(ids: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("today_routines").document(todayDate)
            .set(mapOf("routineIds" to ids))
    }

    // RecyclerView에 표시할 리스트와 체크 상태를 셋팅하고 UI 갱신
    private fun updateRoutineAdapterAndUI() {
        val listToShow = if (showingAll) todayRoutineList else todayRoutineList.take(3)
        routineAdapter.updateList(listToShow)
        routineAdapter.setCheckedMap(checkedStatus)
        updateAchievementRate(todayRoutineList)
        showMoreBtn.visibility = if (todayRoutineList.isEmpty()) View.GONE else View.VISIBLE
    }

    // 목표 달성률 계산 및 표시
    private fun updateAchievementRate(routines: List<Routine>) {
        val completed = routines.count { checkedStatus[it.id] == true }
        val percent = if (routines.isNotEmpty()) (completed * 100 / routines.size) else 0
        achievementRateTextView.text = "목표 달성률: $percent%"
    }

    // 체중 정보를 Firestore에 저장 (덮어쓰기)
    private fun saveWeightToFirestore(weight: Double) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid).collection("dailyRecords").document(todayDate)

        docRef.get().addOnSuccessListener { doc ->
            val data = doc.data?.toMutableMap() ?: mutableMapOf()
            data["weight"] = weight
            docRef.set(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "체중 입력 완료", Toast.LENGTH_SHORT).show()
                    weightInput.text.clear()
                }
        }
    }

    // 운동 내역을 Firestore에 저장 (기존 데이터에 추가)
    private fun saveExerciseToFirestore(name: String, time: String) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid).collection("dailyRecords").document(todayDate)

        val newExercise = mapOf("exerciseName" to name, "exerciseTime" to time)

        docRef.get().addOnSuccessListener { doc ->
            val exercises = (doc.get("exercises") as? MutableList<Map<String, String>> ?: mutableListOf())
            exercises.add(newExercise)
            docRef.update("exercises", exercises)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "운동내역 입력 완료", Toast.LENGTH_SHORT).show()
                    exerciseNameEditText.text.clear()
                    exerciseTimeEditText.text.clear()
                }.addOnFailureListener {
                    docRef.set(mapOf("exercises" to listOf(newExercise)))
                }
        }
    }

    // 루틴 삭제 확인 다이얼로그 표시 및 삭제 처리
    private fun showDeleteDialog(routine: Routine) {
        AlertDialog.Builder(requireContext())
            .setTitle("루틴 삭제")
            .setMessage("루틴 \"${routine.routineName}\" 을(를) 목록에서 제거하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // todayRoutineList에서 제거 및 UI 갱신
                todayRoutineList.remove(routine)
                updateRoutineAdapterAndUI()

                val currentUser = FirebaseAuth.getInstance().currentUser
                val todayDate = todayDate  // 예: "2025-07-29"
                currentUser?.let { user ->
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(user.uid)
                        .collection("today_routines")
                        .document(todayDate)
                        .update("routineIds", FieldValue.arrayRemove(routine.id))
                        .addOnSuccessListener {
                            Log.d("Firestore", "루틴 삭제 성공: ${routine.routineName}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "루틴 삭제 실패", e)
                        }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
