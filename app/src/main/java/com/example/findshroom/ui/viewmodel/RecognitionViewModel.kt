package com.example.findshroom.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findshroom.data.model.Mushroom
import com.example.findshroom.data.repository.HuggingFaceRepository
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
    private val huggingFaceRepository: HuggingFaceRepository,
    private val mushroomRepository: MushroomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecognitionUiState())
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

    fun recognizeMushroom(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            huggingFaceRepository.recognizeMushroom(bitmap)
                .onSuccess { response ->
                    Log.d("HF_DEBUG", "response = $response")
                    try {
                        val json = JSONObject(response)

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
                        Log.e("HF_DEBUG", "JSON parse error", e)
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
                    Log.e("HF_DEBUG", "recognize error", error)
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
}