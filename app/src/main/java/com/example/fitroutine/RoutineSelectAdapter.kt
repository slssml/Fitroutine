package com.example.fitroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 루틴 선택용 어댑터
class RoutineSelectAdapter(
    private var routineList: List<Routine>,                                 // 표시할 루틴 리스트
    private val selectedMap: MutableMap<String, Boolean> = mutableMapOf(),  // 루틴 체크 상태 저장
    private var onItemClick: ((Routine) -> Unit)? = null,                   // 아이템 클릭 시 호출되는 콜백
    private val showCheckbox: Boolean = true                                // 체크박스 표시 여부
) : RecyclerView.Adapter<RoutineSelectAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineText: TextView = itemView.findViewById(R.id.routineText)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_routine, parent, false)  // 같은 레이아웃 재사용 가능
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = routineList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routineList[position]

        // 루틴 이름, 시간, 요일 정보를 텍스트로 표시
        holder.routineText.text =
            "${routine.routineName} / ${routine.time} / ${routine.days.joinToString(", ")}"

        // 체크박스 표시 여부 설정
        holder.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedMap[routine.id] ?: false

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            selectedMap[routine.id] = isChecked
        }

        // 아이템 클릭 시 콜백 호출
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(routine)
        }
    }
}
