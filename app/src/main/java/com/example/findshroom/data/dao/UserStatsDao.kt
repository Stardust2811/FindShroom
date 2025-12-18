package com.example.findshroom.data.dao

import androidx.room.*
import com.example.findshroom.data.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    suspend fun getUserStats(userId: Long): UserStats?
    
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStatsFlow(userId: Long): Flow<UserStats?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)
    
    @Update
    suspend fun updateUserStats(stats: UserStats)
    
    @Query("UPDATE user_stats SET experience = experience + :exp, totalMushroomsCollected = totalMushroomsCollected + :mushrooms WHERE userId = :userId")
    suspend fun addExperience(userId: Long, exp: Int, mushrooms: Int)
}

