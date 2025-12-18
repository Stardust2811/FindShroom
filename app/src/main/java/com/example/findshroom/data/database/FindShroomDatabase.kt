package com.example.findshroom.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findshroom.data.dao.*
import com.example.findshroom.data.model.*

@Database(
    entities = [
        Mushroom::class,
        MapMarker::class,
        User::class,
        Subscription::class,
        UserStats::class,
        DiaryEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FindShroomDatabase : RoomDatabase() {
    abstract fun mushroomDao(): MushroomDao
    abstract fun mapMarkerDao(): MapMarkerDao
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun diaryEntryDao(): DiaryEntryDao
}

