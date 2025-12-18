package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String, // В реальном приложении должен быть хеш
    val email: String? = null,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

