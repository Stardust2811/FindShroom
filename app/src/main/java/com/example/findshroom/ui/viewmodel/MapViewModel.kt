package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.MapMarker
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
    val selectedMarker: MapMarker? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapMarkerRepository: MapMarkerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadMarkers()
    }
    
    private fun loadMarkers() {
        viewModelScope.launch {
            try {
                mapMarkerRepository.getAllMarkers()
                    .collect { markers ->
                        _uiState.value = _uiState.value.copy(markers = markers)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка загрузки меток: ${e.message}",
                    markers = emptyList()
                )
            }
        }
    }
    
    fun addMarker(latitude: Double, longitude: Double, photoUri: String, note: String? = null) {
        viewModelScope.launch {
            try {
                val marker = MapMarker(
                    latitude = latitude,
                    longitude = longitude,
                    photoUri = photoUri,
                    note = note
                )
                mapMarkerRepository.insertMarker(marker)
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
}

