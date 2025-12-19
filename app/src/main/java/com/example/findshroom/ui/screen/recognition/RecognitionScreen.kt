package com.example.findshroom.ui.screen.recognition

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.findshroom.ui.viewmodel.RecognitionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecognitionScreen(
    viewModel: RecognitionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA
        )
    )

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            capturedImageUri = it
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                    capturedBitmap = bitmap
                    viewModel.recognizeMushroom(bitmap)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            photoFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                capturedImageUri = uri
                BitmapFactory.decodeFile(file.absolutePath)?.let { bitmap ->
                    capturedBitmap = bitmap
                    viewModel.recognizeMushroom(bitmap)
                }
            }
        }
    }

    fun takePhoto() {
        val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        photoFile = file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FindShroom",
            style = MaterialTheme.typography.headlineMedium
        )

        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Captured image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Fit
            )
        } else if (capturedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(capturedImageUri)
                        .build()
                ),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        takePhoto()
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Камера")
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Галерея")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Text("Распознавание...")
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        uiState.recognizedMushroom?.let { mushroom ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (mushroom.isEdible) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mushroom.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (mushroom.scientificName.isNotEmpty()) {
                        Text(
                            text = mushroom.scientificName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (mushroom.isEdible) "Съедобный" else "Несъедобный",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (mushroom.isEdible) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = mushroom.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    mushroom.habitat?.let {
                        Text(
                            text = "Место обитания: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    mushroom.season?.let {
                        Text(
                            text = "Сезон: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveMushroom(mushroom, capturedImageUri?.toString())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить в справочник")
                    }
                }
            }
        }
    }
}