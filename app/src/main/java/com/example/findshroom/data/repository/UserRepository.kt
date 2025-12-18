package com.example.findshroom.data.repository

import com.example.findshroom.data.dao.UserDao
import com.example.findshroom.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}

