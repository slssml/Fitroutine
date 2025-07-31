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

// 유튜브 영상 목록을 리사이클뷰에 표시하기 위한 어댑터 클래스
class VideoAdapter(
    private val context: Context,           // 프래그먼트 컨텍스트
    private var items: List<VideoItem>,     // 표시할 영상 리스트

    private val hideAddButton: Boolean = false,                    // + 버튼 슴김 여부 플래그
    var onItemLongClick: ((VideoItem) -> Unit)? = null,    // 아이템 롱클릭 시 콜백
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    // ViewHolder 정의
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)
        val title: TextView = itemView.findViewById(R.id.youtubeTitle)
        val addButton: ImageButton = itemView.findViewById(R.id.routineAdd)

        // 아이템 롱클릭 onItemLongClick 콜백 호출
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val video = items[position]
                    // 둘 다 호출할 수도 있고, 하나만 호출해도 됨
                    onItemLongClick?.invoke(video)
                }
                true
            }
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        // 유튜브 영상 ID 추출 후 썸네일 URL 생성
        val videoId = extractYoutubeId(item.youtubeUrl)
        val thumbnailUrl = videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

        // Glide로 썸네일 이미지 로드
        if (thumbnailUrl != null) {
            Glide.with(context)
                .load(thumbnailUrl)
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.setImageResource(R.drawable.default_thumbnail)
        }

        // 썸네일 클릭 → 유튜브 영상 열기
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

    // 총 아이템 개수 리턴
    override fun getItemCount(): Int = items.size

    // 유튜브 URL에서 ID 추출
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

        // 다이얼로그 내 버튼 숨김 처리
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddSelected)
        btnCancel.visibility = View.GONE
        btnAdd.visibility = View.GONE

        // Firestore에서 유저 루틴 목록 불러오기
        val routineRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("routines")

        routineRef.get().addOnSuccessListener { snapshot ->
            // 루틴 목록을 Routine 객체 리스트로 변환
            val routines = snapshot.map { doc ->
                Routine(
                    id = doc.id,
                    routineName = doc.getString("routineName") ?: "이름 없음",
                    time = doc.getString("time") ?: "",
                    days = (doc.get("days") as? List<String>) ?: emptyList()
                )
            }

            // 다이얼로그 내 리사이클뷰에 루틴 목록 표시
            val adapter = RoutineSelectAdapter(
                routineList = routines,
                showCheckbox = false,
                onItemClick = { selectedRoutine ->

                    // 루틴 선택 시 해당 루틴에 영상 추가
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

    // 선택한 루틴에 영상 정보를 Firestore에 저장
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

    fun removeItem(item: VideoItem) {
        items = items.filter { it != item }
        notifyDataSetChanged()
    }

    // 외부에서 리스트 업데이트 할 때 사용
    fun updateData(newItems: List<VideoItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
