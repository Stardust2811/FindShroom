package com.example.findshroom.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.MapMarker
import com.example.findshroom.data.repository.LocationRepository
import com.example.findshroom.data.repository.MapMarkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMarker: MapMarker? = null,
    val userLocation: Location? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapMarkerRepository: MapMarkerRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: com.example.findshroom.data.repository.AuthRepository,
    private val subscriptionRepository: com.example.findshroom.data.repository.SubscriptionRepository,
    private val userStatsRepository: com.example.findshroom.data.repository.UserStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadMarkers()
        getCurrentLocation()
    }
    
    private fun loadMarkers() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val hasSubscription = userId?.let { subscriptionRepository.getActiveSubscription(it) != null } ?: false
            
            mapMarkerRepository.getAllMarkers()
                .collect { allMarkers ->
                    val filteredMarkers = if (hasSubscription && userId != null) {
                        // Платные пользователи видят все метки
                        allMarkers
                    } else {
                        // Бесплатные пользователи видят только публичные метки
                        allMarkers.filter { !it.isPrivate }
                    }
                    _uiState.value = _uiState.value.copy(markers = filteredMarkers)
                }
        }
    }
    
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val location = locationRepository.getCurrentLocation()
            _uiState.value = _uiState.value.copy(
                userLocation = location,
                isLoading = false
            )
        }
    }
    
    fun addMarker(latitude: Double, longitude: Double, photoUri: String, title: String? = null, note: String? = null, isPrivate: Boolean = false) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                val hasSubscription = userId?.let { subscriptionRepository.getActiveSubscription(it) != null } ?: false
                
                val marker = MapMarker(
                    latitude = latitude,
                    longitude = longitude,
                    photoUri = photoUri,
                    title = title,
                    note = note,
                    userId = userId,
                    isPrivate = isPrivate && hasSubscription
                )
                mapMarkerRepository.insertMarker(marker)
                
                userId?.let { userStatsRepository.incrementMarkersCreated(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка добавления метки: ${e.message}"
                )
            }
        }
    }
    
    fun updateMarker(marker: MapMarker) {
        viewModelScope.launch {
            try {
                mapMarkerRepository.updateMarker(marker)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка обновления метки: ${e.message}"
                )
            }
        }
    }
    
    fun deleteMarker(marker: MapMarker) {
        viewModelScope.launch {
            try {
                mapMarkerRepository.deleteMarker(marker)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка удаления метки: ${e.message}"
                )
            }
        }
    }
    
    fun selectMarker(marker: MapMarker?) {
        _uiState.value = _uiState.value.copy(selectedMarker = marker)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun checkSubscription(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            val hasSub = userId?.let { subscriptionRepository.getActiveSubscription(it) != null } ?: false
            callback(hasSub)
        }
    }
}

