package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.MapMarker
import com.example.findshroom.data.repository.AuthRepository
import com.example.findshroom.data.repository.MapMarkerRepository
import com.example.findshroom.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val markers: List<MapMarker> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mapMarkerRepository: MapMarkerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    
    init {
        checkAdminAccess()
        loadMarkers()
    }
    
    private fun checkAdminAccess() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val user = userRepository.getUserById(userId)
            _uiState.value = _uiState.value.copy(isAdmin = user?.isAdmin == true)
        }
    }
    
    private fun loadMarkers() {
        viewModelScope.launch {
            mapMarkerRepository.getAllMarkers().collect { markers ->
                _uiState.value = _uiState.value.copy(markers = markers)
            }
        }
    }
    
    fun deleteMarker(marker: MapMarker) {
        viewModelScope.launch {
            mapMarkerRepository.deleteMarker(marker)
        }
    }
}

