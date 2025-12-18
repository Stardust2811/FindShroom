package com.example.findshroom.data.dao

import androidx.room.*
import com.example.findshroom.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDiaryEntries(userId: Long): Flow<List<DiaryEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntry): Long
    
    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntry)
    
    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntry)
}

