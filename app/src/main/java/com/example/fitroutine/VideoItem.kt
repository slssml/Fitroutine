package com.example.fitroutine

data class VideoItem (
    val id: Int = 0,        // id 자동 생성
    val title: String,      // 유튜브 영상 제목
    val youtubeUrl: String, // 유튜브 영상 주소
    val category: String    // 영상 카테고리
)