package com.example.findshroom.data.dao

import androidx.room.*
import com.example.findshroom.data.model.Mushroom
import kotlinx.coroutines.flow.Flow

@Dao
interface MushroomDao {
    @Query("SELECT * FROM mushrooms ORDER BY name ASC")
    fun getAllMushrooms(): Flow<List<Mushroom>>
    
    @Query("SELECT * FROM mushrooms WHERE id = :id")
    suspend fun getMushroomById(id: Long): Mushroom?
    
    @Query("SELECT * FROM mushrooms WHERE name LIKE :query OR scientificName LIKE :query")
    fun searchMushrooms(query: String): Flow<List<Mushroom>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMushroom(mushroom: Mushroom): Long
    
    @Update
    suspend fun updateMushroom(mushroom: Mushroom)
    
    @Delete
    suspend fun deleteMushroom(mushroom: Mushroom)
}

