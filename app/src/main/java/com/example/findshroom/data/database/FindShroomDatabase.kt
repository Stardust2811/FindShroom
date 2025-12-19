package com.example.findshroom.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findshroom.data.dao.MapMarkerDao
import com.example.findshroom.data.dao.MushroomDao
import com.example.findshroom.data.model.MapMarker
import com.example.findshroom.data.model.Mushroom

@Database(
    entities = [Mushroom::class, MapMarker::class],
    version = 3,
    exportSchema = false
)
abstract class FindShroomDatabase : RoomDatabase() {
    abstract fun mushroomDao(): MushroomDao
    abstract fun mapMarkerDao(): MapMarkerDao
}

