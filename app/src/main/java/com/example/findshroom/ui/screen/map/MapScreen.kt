package com.example.findshroom.ui.screen.map

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.findshroom.data.model.MapMarker
import com.example.findshroom.ui.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showAddMarkerDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Point?>(null) }
    var markerNote by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
        }
    }

    val defaultLocation = Point(55.7558, 37.6173) // Moscow

    // Single MapView instance tied to composable lifecycle
    val mapView = remember {
        try {
            MapView(context).apply {
                map.move(
                    CameraPosition(defaultLocation, 10f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Remember reference so we can read camera position from FAB
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    var inputListener by remember { mutableStateOf<InputListener?>(null) }

    // Handle MapView lifecycle
    DisposableEffect(lifecycle, mapView) {
        if (mapView == null) {
            onDispose { }
        } else {
            val observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    try {
                        // MapKit global lifecycle + MapView lifecycle
                        MapKitFactory.getInstance().onStart()
                        mapView.onStart()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    try {
                        mapView.onStop()
                        MapKitFactory.getInstance().onStop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            lifecycle.addObserver(observer)

            onDispose {
                try {
                    lifecycle.removeObserver(observer)
                    inputListener?.let { mapView.map.removeInputListener(it) }
                    mapView.onStop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (mapView == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ошибка инициализации карты")
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    mapViewState.value = this

                    // Long tap to choose location
                    val listener = object : InputListener {
                        override fun onMapTap(map: Map, point: Point) {
                            // ignore single tap
                        }

                        override fun onMapLongTap(map: Map, point: Point) {
                            selectedLocation = point
                            showAddMarkerDialog = true
                        }
                    }
                    inputListener = listener
                    map.addInputListener(listener)
                }
            },
            update = { view ->
                mapViewState.value = view

                try {
                    val mapObjects = view.map.mapObjects
                    mapObjects.clear()

                    uiState.markers.forEach { marker ->
                        try {
                            val point = Point(marker.latitude, marker.longitude)
                            val placemark = mapObjects.addPlacemark(point)
                            placemark.userData = marker

                            placemark.addTapListener(MapObjectTapListener { _, _ ->
                                viewModel.selectMarker(marker)
                                true
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Закрыть")
                    }
                }
            }
        }

        // Floating action button – takes current camera center
        FloatingActionButton(
            onClick = {
                mapViewState.value?.map?.cameraPosition?.target?.let { location ->
                    selectedLocation = location
                    showAddMarkerDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить метку")
        }

        // Marker details dialog
        uiState.selectedMarker?.let { marker ->
            MarkerDetailsDialog(
                marker = marker,
                onDismiss = { viewModel.selectMarker(null) },
                onDelete = {
                    viewModel.deleteMarker(marker)
                    viewModel.selectMarker(null)
                },
                onUpdate = { updatedMarker ->
                    viewModel.updateMarker(updatedMarker)
                    viewModel.selectMarker(null)
                }
            )
        }

        // Add marker dialog
        if (showAddMarkerDialog) {
            AddMarkerDialog(
                location = selectedLocation,
                note = markerNote,
                photoUri = selectedPhotoUri,
                onNoteChange = { markerNote = it },
                onPhotoSelect = { imagePickerLauncher.launch("image/*") },
                onDismiss = {
                    showAddMarkerDialog = false
                    markerNote = ""
                    selectedPhotoUri = null
                    selectedLocation = null
                },
                onConfirm = { location, note, photoUri ->
                    if (location != null && photoUri != null) {
                        viewModel.addMarker(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            photoUri = photoUri.toString(),
                            note = note.ifEmpty { null }
                        )
                        showAddMarkerDialog = false
                        markerNote = ""
                        selectedPhotoUri = null
                        selectedLocation = null
                    }
                }
            )
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerDialog(
    location: Point?,
    note: String,
    photoUri: Uri?,
    onNoteChange: (String) -> Unit,
    onPhotoSelect: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Point?, String, Uri?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить метку") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Местоположение: ${location?.latitude}, ${location?.longitude}")

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onPhotoSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (photoUri != null) "Фото выбрано" else "Выбрать фото")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(location, note, photoUri) },
                enabled = location != null && photoUri != null
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerDetailsDialog(
    marker: MapMarker,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (MapMarker) -> Unit
) {
    var note by remember { mutableStateOf(marker.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Детали метки") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Координаты: ${marker.latitude}, ${marker.longitude}")

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUpdate(marker.copy(note = note.ifEmpty { null })) }
                ) {
                    Text("Сохранить")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

