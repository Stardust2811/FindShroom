package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val subscriptionKey: String,
    val isActive: Boolean = true,
    val activatedAt: Long = System.currentTimeMillis()
)

