package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val note: String,
    val mushroomsCollected: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

