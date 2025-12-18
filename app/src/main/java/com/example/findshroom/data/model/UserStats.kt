package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val userId: Long,
    val experience: Int = 0,
    val level: Int = 1,
    val totalMushroomsCollected: Int = 0,
    val totalMarkersCreated: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getLevelTitle(): String {
        return when {
            level >= 20 -> "Профессиональный грибник"
            level >= 15 -> "Опытный грибник"
            level >= 10 -> "Продвинутый грибник"
            level >= 5 -> "Любитель"
            else -> "Новичок"
        }
    }
    
    fun getExperienceForNextLevel(): Int {
        return level * 100
    }
}

