package com.example.fitroutine

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class VideoDBHelper(context: Context) : SQLiteOpenHelper(context, "video_db", null, 1){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE videos (id INTEGER PRIMARY KEY AUTOINCREMENT, 
            title TEXT NOT NULL, 
            youtubeUrl TEXT NOT NULL, 
            category TEXT NOT NULL) 
            """.trimIndent()
        )

        db.execSQL("""
        CREATE TABLE routine_video (
            routineId TEXT NOT NULL,
            videoId INTEGER NOT NULL,
            PRIMARY KEY (routineId, videoId)
        )
    """.trimIndent())

    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS videos")
        onCreate(db)
    }
}