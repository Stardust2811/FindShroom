package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.User
import com.example.findshroom.data.model.UserStats
import com.example.findshroom.data.repository.AuthRepository
import com.example.findshroom.data.repository.SubscriptionRepository
import com.example.findshroom.data.repository.UserRepository
import com.example.findshroom.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val stats: UserStats? = null,
    val hasSubscription: Boolean = false,
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val user = userRepository.getUserById(userId)
            val stats = userStatsRepository.getUserStats(userId)
            val subscription = subscriptionRepository.getActiveSubscription(userId)
            
            _uiState.value = _uiState.value.copy(
                user = user,
                stats = stats,
                hasSubscription = subscription != null,
                isAdmin = user?.isAdmin == true,
                isLoading = false
            )
            
            userStatsRepository.getUserStatsFlow(userId).collect { updatedStats ->
                _uiState.value = _uiState.value.copy(stats = updatedStats)
            }
        }
    }
}

