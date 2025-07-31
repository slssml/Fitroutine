package com.example.fitroutine
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 영상 데이터 클래스
@Parcelize
data class VideoItem(
    val id: Int = 0,
    val title: String,
    val youtubeUrl: String,
    val category: String
) : Parcelable
