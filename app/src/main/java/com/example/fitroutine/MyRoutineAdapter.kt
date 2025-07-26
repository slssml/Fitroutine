package com.example.fitroutine

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRoutineAdapter(private var routineList: List<String>) : RecyclerView.Adapter<MyRoutineAdapter.ViewHolder>() {

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun getItemCount(): Int = routineList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = routineList[position]
    }

    fun updateList(newList: List<String>) {
        routineList = newList
        notifyDataSetChanged()
    }
}


