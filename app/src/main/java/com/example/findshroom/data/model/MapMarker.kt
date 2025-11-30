package com.example.findshroom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_markers")
data class MapMarker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String,
    val mushroomId: Long? = null, // Reference to mushroom if recognized
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

