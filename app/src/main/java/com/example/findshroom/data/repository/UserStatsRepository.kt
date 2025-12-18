package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.UserStatsDao
import com.example.findshroom.data.model.UserStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserStatsRepository @Inject constructor(
    private val userStatsDao: UserStatsDao
) {
    suspend fun getUserStats(userId: Long): UserStats? = userStatsDao.getUserStats(userId)
    
    fun getUserStatsFlow(userId: Long): Flow<UserStats?> = userStatsDao.getUserStatsFlow(userId)
    
    suspend fun getOrCreateUserStats(userId: Long): UserStats {
        var stats = getUserStats(userId)
        if (stats == null) {
            stats = UserStats(userId = userId)
            userStatsDao.insertUserStats(stats)
        }
        return stats
    }
    
    suspend fun addExperience(userId: Long, exp: Int, mushrooms: Int) {
        val stats = getOrCreateUserStats(userId)
        var newExp = stats.experience + exp
        var newLevel = stats.level
        
        while (newExp >= newLevel * 100) {
            newExp -= newLevel * 100
            newLevel++
        }
        
        userStatsDao.updateUserStats(
            stats.copy(
                experience = newExp,
                level = newLevel,
                totalMushroomsCollected = stats.totalMushroomsCollected + mushrooms
            )
        )
    }
    
    suspend fun incrementMarkersCreated(userId: Long) {
        val stats = getOrCreateUserStats(userId)
        userStatsDao.updateUserStats(
            stats.copy(totalMarkersCreated = stats.totalMarkersCreated + 1)
        )
    }
}

