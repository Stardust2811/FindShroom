package com.example.findshroom.data.dao

import androidx.room.*
import com.example.findshroom.data.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveSubscription(userId: Long): Subscription?
    
    @Query("SELECT * FROM subscriptions WHERE subscriptionKey = :key LIMIT 1")
    suspend fun getSubscriptionByKey(key: String): Subscription?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long
    
    @Update
    suspend fun updateSubscription(subscription: Subscription)
}

