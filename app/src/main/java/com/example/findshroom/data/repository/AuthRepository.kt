package com.example.findshroom.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    fun getCurrentUserId(): Long? {
        val userId = prefs.getLong("current_user_id", -1L)
        return if (userId == -1L) null else userId
    }
    
    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong("current_user_id", userId).apply()
    }
    
    fun logout() {
        prefs.edit().remove("current_user_id").apply()
    }
    
    fun isLoggedIn(): Boolean = getCurrentUserId() != null
}

