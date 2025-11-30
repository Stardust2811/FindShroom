package com.example.findshroom.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.Mushroom
import com.example.findshroom.data.repository.GeminiRepository
import com.example.findshroom.data.repository.MushroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class RecognitionUiState(
    val isLoading: Boolean = false,
    val recognizedMushroom: Mushroom? = null,
    val error: String? = null,
    val rawResponse: String? = null
)

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val mushroomRepository: MushroomRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecognitionUiState())
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()
    
    fun recognizeMushroom(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            geminiRepository.recognizeMushroom(bitmap)
                .onSuccess { response ->
                    try {
                        // Try to parse JSON response
                        val jsonString = extractJsonFromResponse(response)
                        val json = JSONObject(jsonString)
                        
                        val mushroom = Mushroom(
                            name = json.optString("name", "Неизвестный гриб"),
                            scientificName = json.optString("scientificName", ""),
                            description = json.optString("description", ""),
                            isEdible = json.optBoolean("isEdible", false),
                            habitat = json.optString("habitat"),
                            season = json.optString("season"),
                            characteristics = json.optString("characteristics")
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            recognizedMushroom = mushroom,
                            rawResponse = response
                        )
                    } catch (e: Exception) {
                        // If JSON parsing fails, create a mushroom from raw response
                        val mushroom = Mushroom(
                            name = "Распознано",
                            scientificName = "",
                            description = response,
                            isEdible = false
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            recognizedMushroom = mushroom,
                            rawResponse = response
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка распознавания"
                    )
                }
        }
    }
    
    fun saveMushroom(mushroom: Mushroom, imageUri: String?) {
        viewModelScope.launch {
            try {
                val mushroomToSave = mushroom.copy(imageUri = imageUri)
                mushroomRepository.insertMushroom(mushroomToSave)
                _uiState.value = _uiState.value.copy(recognizedMushroom = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }
    
    fun clearState() {
        _uiState.value = RecognitionUiState()
    }
    
    private fun extractJsonFromResponse(response: String): String {
        // Try to extract JSON from markdown code blocks or plain text
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        return if (jsonStart >= 0 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd + 1)
        } else {
            response
        }
    }
}

