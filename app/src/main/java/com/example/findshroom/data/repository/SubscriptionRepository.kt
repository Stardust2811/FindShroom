package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.SubscriptionDao
import com.example.findshroom.data.model.Subscription
import javax.inject.Inject

class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend fun getActiveSubscription(userId: Long): Subscription? = 
        subscriptionDao.getActiveSubscription(userId)
    
    suspend fun getSubscriptionByKey(key: String): Subscription? = 
        subscriptionDao.getSubscriptionByKey(key)
    
    suspend fun activateSubscription(userId: Long, key: String): Boolean {
        val existing = subscriptionDao.getSubscriptionByKey(key)
        return if (existing == null) {
            val subscription = Subscription(userId = userId, subscriptionKey = key)
            subscriptionDao.insertSubscription(subscription)
            true
        } else {
            false
        }
    }
    
    suspend fun insertSubscription(subscription: Subscription): Long = 
        subscriptionDao.insertSubscription(subscription)
}

