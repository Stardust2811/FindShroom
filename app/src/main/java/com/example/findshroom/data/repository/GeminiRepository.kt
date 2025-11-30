package com.example.findshroom.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeminiRepository @Inject constructor() {
    private val apiKey = "AIzaSyBK5s27BPFs44j2xR-s-qejFHgsgc4QItI"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )
    
    suspend fun recognizeMushroom(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Проанализируй это изображение гриба и предоставь информацию в следующем формате JSON:
                {
                    "name": "Название гриба на русском",
                    "scientificName": "Научное название на латыни",
                    "isEdible": true/false,
                    "description": "Подробное описание гриба",
                    "habitat": "Где растет",
                    "season": "Сезон сбора",
                    "characteristics": "Отличительные характеристики"
                }
                
                Если это не гриб, верни JSON с isEdible: false и description: "На изображении не обнаружен гриб"
            """.trimIndent()
            
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            
            val text = response.text ?: throw Exception("Пустой ответ от AI")
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

