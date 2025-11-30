package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mushrooms")
data class Mushroom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val scientificName: String,
    val description: String,
    val isEdible: Boolean,
    val imageUri: String? = null,
    val habitat: String? = null,
    val season: String? = null,
    val characteristics: String? = null
)

