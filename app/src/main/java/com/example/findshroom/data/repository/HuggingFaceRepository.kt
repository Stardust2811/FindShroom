package com.example.findshroom.data.repository

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HuggingFaceRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun recognizeMushroom(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            val imageBytes = ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.toByteArray()
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "mushroom.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(SPACE_API_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: $errorBody")
                    )
                }

                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Пустой ответ от сервера"))

                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val SPACE_API_URL =
            "https://stardust2811-findshroomapi.hf.space/predict"
    }
}