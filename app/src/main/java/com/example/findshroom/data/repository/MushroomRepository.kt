package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.MushroomDao
import com.example.findshroom.data.model.Mushroom
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MushroomRepository @Inject constructor(
    private val mushroomDao: MushroomDao
) {
    fun getAllMushrooms(): Flow<List<Mushroom>> = mushroomDao.getAllMushrooms()
    
    suspend fun getMushroomById(id: Long): Mushroom? = mushroomDao.getMushroomById(id)
    
    fun searchMushrooms(query: String): Flow<List<Mushroom>> = mushroomDao.searchMushrooms("%$query%")
    
    suspend fun insertMushroom(mushroom: Mushroom): Long = mushroomDao.insertMushroom(mushroom)
    
    suspend fun updateMushroom(mushroom: Mushroom) = mushroomDao.updateMushroom(mushroom)
    
    suspend fun deleteMushroom(mushroom: Mushroom) = mushroomDao.deleteMushroom(mushroom)
}

