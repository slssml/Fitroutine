package com.example.fitroutine

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(private val items: List<VideoItem>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    // 영상 항목의 뷰를 저장하고 재사용
    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)
        val title: TextView = itemView.findViewById(R.id.text_title)
    }

    // 아이템 레이아웃을 inflater를 통해 생성하고 ViewHolder로 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    // 각 항목에 데이터 바인딩
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        // 유튜브 주소를 유효한 주소로 변환
        fun extractYoutubeId(url: String): String? {
            val regex = Regex("(?:v=|youtu\\.be/|embed/)([a-zA-Z0-9_-]{11})")
            val match = regex.find(url)
            return match?.groups?.get(1)?.value
        }

        // 유튜브 썸네일 가져오기
        val videoId = extractYoutubeId(item.youtubeUrl)

        if (videoId != null) {  // 성공시 glide를 이용해 썸네일 로딩
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
            Glide.with(holder.thumbnail.context)
                .load(thumbnailUrl)
                .into(holder.thumbnail)
        } else {    // 썸네일 로딩 실패시 기본 썸네일 출력
            holder.thumbnail.setImageResource(R.drawable.default_thumbnail)
        }
    }
    override fun getItemCount() = items.size
}