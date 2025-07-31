package com.example.fitroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 루틴 목록을 RecyclerView로 보여주기 위한 Adapter 클래스
class MyRoutineAdapter(
    private var routineList: MutableList<Routine>,                        // 루틴 목록 데이터
    private var checkedMap: MutableMap<String, Boolean> = mutableMapOf(), // 루틴별 체크 상태 저장
    private val onCheckChanged: (String, Boolean) -> Unit = { _, _ -> },  // 체크 상태 변경 시 호출되는 콜백
    private val showCheckbox: Boolean = true,                             // 체크박스 노출 여부 결정 플래그
    private val onLongClick: ((Routine) -> Unit)? = null,                 // 아이템 롱클릭 시 콜백
    private val onItemClick: ((Routine) -> Unit)? = null                  // 아이템 클릭 시 콜백
) : RecyclerView.Adapter<MyRoutineAdapter.ViewHolder>() {

    // 루틴 뷰의 구성요소
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineText: TextView = itemView.findViewById(R.id.routineText)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxComplete) // 완료 여부 체크박스
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_routine, parent, false)
        return ViewHolder(view)
    }

    // 데이터 리스트 크기
    override fun getItemCount(): Int = routineList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routineList[position]

        // 루틴 이름, 시간, 운동 요일 정보를 TextView에 표시
        holder.routineText.text =
            "${routine.routineName} / ${routine.time} / ${routine.days.joinToString(", ")}"

        // 체크박스 리스너 중복 호출 방지
        holder.checkBox.setOnCheckedChangeListener(null)

        // 체크박스 표시 여부
        holder.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

        // 체크 상태를 checkedMap에서 가져와 설정, 없으면 기본 false
        holder.checkBox.isChecked = checkedMap[routine.id] ?: false

        // 체크박스 상태 변경될 때 호출되는 리스너 등록
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedMap[routine.id] = isChecked
            onCheckChanged(routine.id, isChecked)
        }

        // 루틴 롱클릭 시 콜백 호출
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(routine)
            true
        }

        // 루틴 클릭 시 콜백 호출
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(routine)
        }
    }

    // 외부에서 체크박스 상태 맵을 새로 세팅할 때 사용
    fun setCheckedMap(newMap: Map<String, Boolean>) {
        checkedMap.clear()
        checkedMap.putAll(newMap)
        notifyDataSetChanged()
    }

    // 외부에서 루틴 리스트 전체를 갱신할 때 사용
    fun updateList(newList: List<Routine>) {
        // 기존 checkedMap 상태를 유지하면서 새로운 루틴에 대해서만 false로 초기화
        val newCheckedMap = mutableMapOf<String, Boolean>()
        for (routine in newList) {
            newCheckedMap[routine.id] = checkedMap[routine.id] ?: false
        }

        routineList = newList.toMutableList()
        checkedMap = newCheckedMap
        notifyDataSetChanged()
    }
}

