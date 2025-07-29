package com.example.fitroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoutineSelectAdapter(
    private var routineList: List<Routine>,
    private val selectedMap: MutableMap<String, Boolean> = mutableMapOf()
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

        holder.routineText.text =
            "${routine.routineName} / ${routine.time} / ${routine.days.joinToString(", ")}"

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedMap[routine.id] ?: false

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            selectedMap[routine.id] = isChecked
        }
    }


}
