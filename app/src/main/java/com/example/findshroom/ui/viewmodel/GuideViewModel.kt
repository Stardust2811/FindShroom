package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.Mushroom
import com.example.findshroom.data.repository.MushroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuideUiState(
    val mushrooms: List<Mushroom> = emptyList(),
    val filteredMushrooms: List<Mushroom> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMushroom: Mushroom? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()
    
    init {
        loadMushrooms()
    }
    
    private fun loadMushrooms() {
        viewModelScope.launch {
            mushroomRepository.getAllMushrooms()
                .collect { mushrooms ->
                    _uiState.value = _uiState.value.copy(
                        mushrooms = mushrooms,
                        filteredMushrooms = filterMushrooms(mushrooms, _uiState.value.searchQuery)
                    )
                }
        }
    }
    
    fun searchMushrooms(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredMushrooms = filterMushrooms(_uiState.value.mushrooms, query)
        )
    }
    
    private fun filterMushrooms(mushrooms: List<Mushroom>, query: String): List<Mushroom> {
        if (query.isBlank()) return mushrooms
        val lowerQuery = query.lowercase()
        return mushrooms.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.scientificName.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }
    
    fun selectMushroom(mushroom: Mushroom?) {
        _uiState.value = _uiState.value.copy(selectedMushroom = mushroom)
    }
    
    fun setEditing(isEditing: Boolean) {
        _uiState.value = _uiState.value.copy(isEditing = isEditing)
    }
    
    fun saveMushroom(mushroom: Mushroom) {
        viewModelScope.launch {
            try {
                if (mushroom.id == 0L) {
                    mushroomRepository.insertMushroom(mushroom)
                } else {
                    mushroomRepository.updateMushroom(mushroom)
                }
                _uiState.value = _uiState.value.copy(isEditing = false, selectedMushroom = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }
    
    fun deleteMushroom(mushroom: Mushroom) {
        viewModelScope.launch {
            try {
                mushroomRepository.deleteMushroom(mushroom)
                _uiState.value = _uiState.value.copy(selectedMushroom = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка удаления: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

