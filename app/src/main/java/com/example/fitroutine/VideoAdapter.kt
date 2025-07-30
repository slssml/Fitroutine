package com.example.fitroutine

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VideoAdapter(
    private val context: Context,
    private var items: List<VideoItem>,

    private val hideAddButton: Boolean = false, // + 버튼 숨길 때 사용

) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    // ViewHolder 정의
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)
        val title: TextView = itemView.findViewById(R.id.youtubeTitle)
        val addButton: ImageButton = itemView.findViewById(R.id.routineAdd)
    }

    // View 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        // 썸네일 설정
        val videoId = extractYoutubeId(item.youtubeUrl)
        val thumbnailUrl = videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

        if (thumbnailUrl != null) {
            Glide.with(context)
                .load(thumbnailUrl)
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.setImageResource(R.drawable.default_thumbnail)
        }

        // 썸네일 클릭 → 유튜브 링크 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.youtubeUrl))
            context.startActivity(intent)
        }

        // + 버튼 표시 여부 제어
        if (hideAddButton) {
            holder.addButton.visibility = View.GONE
        } else {
            holder.addButton.visibility = View.VISIBLE
        }


        // + 버튼 클릭 → 루틴 선택 다이얼로그
        holder.addButton.setOnClickListener {
            showRoutineSelectDialog(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // 유튜브 ID 추출
    private fun extractYoutubeId(url: String): String? {
        val regex = Regex("(?:v=|youtu\\.be/|embed/)([a-zA-Z0-9_-]{11})")
        return regex.find(url)?.groups?.get(1)?.value
    }

    // 루틴 선택 다이얼로그
    private fun showRoutineSelectDialog(video: VideoItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_routine_selector, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.routineRecyclerView)

        val dialog = AlertDialog.Builder(context)
            .setTitle("루틴에 추가")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()



        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddSelected)
        btnCancel.visibility = View.GONE
        btnAdd.visibility = View.GONE


        val routineRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("routines")

        routineRef.get().addOnSuccessListener { snapshot ->
            val routines = snapshot.map { doc ->
                Routine(
                    id = doc.id,
                    routineName = doc.getString("routineName") ?: "이름 없음",
                    time = doc.getString("time") ?: "",
                    days = (doc.get("days") as? List<String>) ?: emptyList()
                )
            }

            val adapter = RoutineSelectAdapter(
                routineList = routines,
                showCheckbox = false,
                onItemClick = { selectedRoutine ->

                    addVideoToRoutine(userId, selectedRoutine.id, video)
                    dialog.dismiss()
                }
            )
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter

            dialog.show()
        }.addOnFailureListener {
            Toast.makeText(context, "루틴 목록을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 선택한 루틴에 영상 추가
    private fun addVideoToRoutine(userId: String, routineId: String, video: VideoItem) {
        val videoData = mapOf(
            "title" to video.title,
            "youtubeUrl" to video.youtubeUrl
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .collection("videos")
            .add(videoData)
            .addOnSuccessListener {
                Toast.makeText(context, "루틴에 영상이 추가되었습니다", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "영상 추가 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // 외부에서 리스트 업데이트할 때 사용
    fun updateData(newItems: List<VideoItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
