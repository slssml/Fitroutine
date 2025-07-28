package com.example.fitroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRoutineAdapter(
    private var routineList: List<Routine>,                       // 루틴 목록 데이터
    private var checkedMap: MutableMap<String, Boolean> = mutableMapOf(),  // 루틴별 체크 상태 저장
    private val onCheckChanged: (String, Boolean) -> Unit = { _, _ -> },  // 체크 상태 변경 시 콜백
    private val showCheckbox: Boolean = true                      // 체크박스 노출 여부 제어
) : RecyclerView.Adapter<MyRoutineAdapter.ViewHolder>() {

    // 각 아이템 뷰 홀더
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineText: TextView = itemView.findViewById(R.id.routineText)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxComplete)
    }

    // 아이템 뷰 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_routine, parent, false)
        return ViewHolder(view)
    }

    // 아이템 개수
    override fun getItemCount(): Int = routineList.size

    // 아이템 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routineList[position]

        holder.routineText.text =
            "${routine.routineName} / ${routine.time} / ${routine.days.joinToString(", ")}"
        holder.checkBox.setOnCheckedChangeListener(null) // 기존 리스너 제거

        holder.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = checkedMap[routine.id] ?: false

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedMap[routine.id] = isChecked // 상태 맵 업데이트
            onCheckChanged(routine.id, isChecked) // 콜백 호출
        }
    }

    // 체크 상태 업데이트
    fun setCheckedMap(newMap: Map<String, Boolean>) {
        checkedMap.clear()
        checkedMap.putAll(newMap)
        notifyDataSetChanged()
    }

    // 루틴 리스트 갱신
    fun updateList(newList: List<Routine>) {
        routineList = newList
        notifyDataSetChanged()
    }
}

