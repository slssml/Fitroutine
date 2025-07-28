package com.example.fitroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRoutineAdapter(
    private var routineList: MutableList<Routine>,                       // 루틴 목록 데이터
    private var checkedMap: MutableMap<String, Boolean> = mutableMapOf(),  // 루틴별 체크 상태 저장
    private val onCheckChanged: (String, Boolean) -> Unit = { _, _ -> },  // 체크 상태 변경 시 콜백
    private val showCheckbox: Boolean = true,                            // 체크박스 노출 여부 제어
    private val onLongClick: ((Routine) -> Unit)? = null                 // 롱클릭 시 콜백
) : RecyclerView.Adapter<MyRoutineAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineText: TextView = itemView.findViewById(R.id.routineText)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_routine, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = routineList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = routineList[position]

        holder.routineText.text =
            "${routine.routineName} / ${routine.time} / ${routine.days.joinToString(", ")}"
        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = checkedMap[routine.id] ?: false

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedMap[routine.id] = isChecked
            onCheckChanged(routine.id, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(routine)
            true
        }
    }

    fun setCheckedMap(newMap: Map<String, Boolean>) {
        checkedMap.clear()
        checkedMap.putAll(newMap)
        notifyDataSetChanged()
    }

    fun updateList(newList: List<Routine>) {
        routineList = newList.toMutableList()
        notifyDataSetChanged()
    }
}

