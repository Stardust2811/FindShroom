package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.User
import com.example.findshroom.data.repository.AuthRepository
import com.example.findshroom.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = authRepository.isLoggedIn()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserId()?.let { userId ->
                userRepository.getUserById(userId)?.let { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoggedIn = true
                    )
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = userRepository.getUserByUsername(username)
                if (user != null && user.password == password) {
                    authRepository.setCurrentUserId(user.id)
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Неверный логин или пароль",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка входа: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (userRepository.getUserByUsername(username) != null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Пользователь с таким именем уже существует",
                        isLoading = false
                    )
                    return@launch
                }
                
                val userId = userRepository.insertUser(
                    User(username = username, password = password)
                )
                authRepository.setCurrentUserId(userId)
                userRepository.getUserById(userId)?.let { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка регистрации: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

