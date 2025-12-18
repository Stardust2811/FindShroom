package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.repository.AuthRepository
import com.example.findshroom.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isActivated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()
    
    fun activateSubscription(key: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                _uiState.value = _uiState.value.copy(error = "Необходимо войти в систему")
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val success = subscriptionRepository.activateSubscription(userId, key)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isActivated = true,
                        isLoading = false,
                        successMessage = "Подписка успешно активирована!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Неверный ключ подписки или ключ уже использован",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка активации: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}

