package com.example.fitroutine

import android.content.ContentValues
import android.content.Context

class VideoRepository (context: Context) {
    private val dbHelper = VideoDBHelper(context)
    fun insert(video: VideoItem) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", video.title)
            put("youtubeUrl", video.youtubeUrl)
            put("category", video.category)
        }
        db.insert("videos", null, values)
        db.close()
    }

    fun getVideosByCategory(category: String): List<VideoItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "videos",
            null,
            "category = ?",
            arrayOf(category),
            null, null, null
        )

        val videoList = mutableListOf<VideoItem>()
        while (cursor.moveToNext()) {
            videoList.add(
                VideoItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    youtubeUrl = cursor.getString(cursor.getColumnIndexOrThrow("youtubeUrl")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                )
            )
        }

        cursor.close()
        db.close()
        return videoList
    }

    fun searchVideosByTitle(query: String): List<VideoItem> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "videos",
            null,
            "title LIKE ?",
            arrayOf("%$query%"),
            null, null, null
        )

        val videoList = mutableListOf<VideoItem>()
        while (cursor.moveToNext()) {
            videoList.add(
                VideoItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    youtubeUrl = cursor.getString(cursor.getColumnIndexOrThrow("youtubeUrl")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                )
            )
        }

        cursor.close()
        db.close()
        return videoList
    }
}