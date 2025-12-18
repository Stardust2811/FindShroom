package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.DiaryEntryDao
import com.example.findshroom.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DiaryEntryRepository @Inject constructor(
    private val diaryEntryDao: DiaryEntryDao
) {
    fun getDiaryEntries(userId: Long): Flow<List<DiaryEntry>> = 
        diaryEntryDao.getDiaryEntries(userId)
    
    suspend fun insertDiaryEntry(entry: DiaryEntry): Long = 
        diaryEntryDao.insertDiaryEntry(entry)
    
    suspend fun updateDiaryEntry(entry: DiaryEntry) = 
        diaryEntryDao.updateDiaryEntry(entry)
    
    suspend fun deleteDiaryEntry(entry: DiaryEntry) = 
        diaryEntryDao.deleteDiaryEntry(entry)
}

