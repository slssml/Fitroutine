package com.example.fitroutine

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRoutineAdapter(private val allRoutines: List<String>) : RecyclerView.Adapter<MyRoutineAdapter.ViewHolder>() {
    private var isExpanded = false

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun getItemCount(): Int {
        return if (isExpanded) allRoutines.size else minOf(3, allRoutines.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = allRoutines[position]
    }

    fun expand() {
        isExpanded = true
        notifyDataSetChanged()
    }

    fun collapse() {
        isExpanded = false
        notifyDataSetChanged()
    }

    fun isCurrentlyExpanded(): Boolean = isExpanded
    fun updateList(displayList: List<String>) {

    }
}
