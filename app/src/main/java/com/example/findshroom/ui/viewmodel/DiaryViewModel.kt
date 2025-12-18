package com.example.findshroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.DiaryEntry
import com.example.findshroom.data.repository.AuthRepository
import com.example.findshroom.data.repository.DiaryEntryRepository
import com.example.findshroom.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    
    init {
        loadEntries()
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            diaryEntryRepository.getDiaryEntries(userId).collect { entries ->
                _uiState.value = _uiState.value.copy(entries = entries)
            }
        }
    }
    
    fun addEntry(note: String, mushroomsCollected: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val entry = DiaryEntry(
                userId = userId,
                note = note,
                mushroomsCollected = mushroomsCollected
            )
            diaryEntryRepository.insertDiaryEntry(entry)
            
            if (mushroomsCollected > 0) {
                userStatsRepository.addExperience(userId, mushroomsCollected * 10, mushroomsCollected)
            }
        }
    }
}

