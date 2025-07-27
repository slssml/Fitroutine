package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var greetingText: TextView
    private lateinit var routineRecyclerView: RecyclerView
    private lateinit var showMoreBtn: Button
    private lateinit var achievementRateTextView: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 오늘 날짜와 요일 구하기
    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val todayKoreanDay = getTodayKoreanDay()

    private val routineList = mutableListOf<Routine>()            // 전체 루틴 목록
    private val checkedStatus = mutableMapOf<String, Boolean>()  // 루틴별 체크 상태

    private var showingAll = false    // 전체 보기 모드 플래그

    private lateinit var routineAdapter: MyRoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 뷰 초기화
        greetingText = view.findViewById(R.id.greetingText)
        routineRecyclerView = view.findViewById(R.id.routineRecyclerView)
        showMoreBtn = view.findViewById(R.id.showMoreBtn)
        achievementRateTextView = view.findViewById(R.id.achievementRateTextView)

        // 어댑터 초기화 (체크박스 보임)
        routineAdapter = MyRoutineAdapter(
            routineList,
            checkedMap = mutableMapOf(),
            onCheckChanged = { routineId, isChecked ->
                saveRoutineStatus(routineId, isChecked)
            },
            showCheckbox = true
        )

        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = routineAdapter

        // 사용자 이름 불러와 인사말 표시
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    greetingText.text = "${name ?: "사용자"}님, 오늘 하루도 파이팅!"
                }
        }

        // 버튼 초기 텍스트 설정
        showMoreBtn.text = "더보기"

        // 더보기 / 접기 토글 버튼 클릭 이벤트
        showMoreBtn.setOnClickListener {
            showingAll = !showingAll
            updateRoutineAdapterAndUI()
            showMoreBtn.text = if (showingAll) "접기" else "더보기"
        }

        // 루틴 목록 불러오기 시작
        updateRoutineList()
    }

    // Firestore에서 루틴 불러오기
    private fun updateRoutineList() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("routines")
            .get()
            .addOnSuccessListener { result ->
                routineList.clear()
                for (doc in result.documents) {
                    val routine = doc.toObject(Routine::class.java)
                    if (routine != null) routineList.add(routine)
                }
                loadRoutineStatus(routineList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "루틴 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 오늘 날짜 기준으로 체크 상태 불러오기
    private fun loadRoutineStatus(routines: List<Routine>) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)
            .collection("routine_status").document(todayDate)

        docRef.get()
            .addOnSuccessListener { document ->
                checkedStatus.clear()
                val data = document.data ?: emptyMap<String, Any>()
                for (routine in routines) {
                    val checked = data[routine.id] as? Boolean ?: false
                    checkedStatus[routine.id] = checked
                }
                updateRoutineAdapterAndUI()
            }
            .addOnFailureListener {
                checkedStatus.clear()
                for (routine in routines) {
                    checkedStatus[routine.id] = false
                }
                updateRoutineAdapterAndUI()
            }
    }

    // 어댑터에 데이터 적용하고 UI 갱신
    private fun updateRoutineAdapterAndUI() {
        val filteredList = if (showingAll) {
            routineList   // 전체 루틴 보여줌
        } else {
            routineList.filter { it.days.contains(todayKoreanDay) }  // 오늘 요일 루틴만 필터링
        }

        routineAdapter.updateList(filteredList)
        routineAdapter.setCheckedMap(checkedStatus)

        updateAchievementRate(filteredList)

        // 루틴 리스트가 비어있을 때만 버튼 숨김
        showMoreBtn.visibility = if (routineList.isEmpty()) View.GONE else View.VISIBLE

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "오늘 루틴이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 체크박스 상태 변경 시 Firestore에 저장
    private fun saveRoutineStatus(routineId: String, isChecked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)
            .collection("routine_status").document(todayDate)

        checkedStatus[routineId] = isChecked

        docRef.set(checkedStatus)
            .addOnSuccessListener {
                updateRoutineAdapterAndUI()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "달성 상태 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // 달성률 계산 후 텍스트 업데이트
    private fun updateAchievementRate(todayRoutines: List<Routine>) {
        if (todayRoutines.isEmpty()) {
            achievementRateTextView.text = "목표 달성률: 0%"
            return
        }

        val completed = todayRoutines.count { checkedStatus[it.id] == true }
        val percent = (completed * 100) / todayRoutines.size
        achievementRateTextView.text = "목표 달성률: $percent%"
    }

    // 오늘 요일 한글 반환
    private fun getTodayKoreanDay(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }
}

