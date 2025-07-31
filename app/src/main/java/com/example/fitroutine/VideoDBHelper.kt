package com.example.fitroutine

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite 데이터베이스 관리하는 헬퍼
class VideoDBHelper(context: Context) : SQLiteOpenHelper(context, "video_db", null, 1){

    override fun onCreate(db: SQLiteDatabase) {
        // videos 테이블 생성
        db.execSQL("""CREATE TABLE videos (id INTEGER PRIMARY KEY AUTOINCREMENT, 
            title TEXT NOT NULL, 
            youtubeUrl TEXT NOT NULL, 
            category TEXT NOT NULL) 
            """.trimIndent()
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS videos")
        onCreate(db)
    }
}