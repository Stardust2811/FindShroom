package com.example.findshroom.ui.screen.map

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
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
    var markerTitle by remember { mutableStateOf("") }
    var markerNote by remember { mutableStateOf("") }
    var markerIsPrivate by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var hasMovedToUserLocation by remember { mutableStateOf(false) }
    
    // Check subscription status
    val hasSubscription = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.checkSubscription { hasSub ->
            hasSubscription.value = hasSub
        }
    }

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
        MapView(context).apply {
            map.move(
                CameraPosition(defaultLocation, 10f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0f),
                null
            )
        }
    }

    // Handle MapView lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // MapKit global lifecycle + MapView lifecycle
                MapKitFactory.getInstance().onStart()
                mapView.onStart()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
        }
    }

    // Remember reference so we can read camera position from FAB
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    // Move to user location when available
    LaunchedEffect(uiState.userLocation, hasMovedToUserLocation) {
        uiState.userLocation?.let { location ->
            if (!hasMovedToUserLocation) {
                val userPoint = Point(location.latitude, location.longitude)
                mapView.map.move(
                    CameraPosition(userPoint, 15f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
                hasMovedToUserLocation = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    mapViewState.value = this

                    // Long tap to choose location
                    map.addInputListener(object : InputListener {
                        override fun onMapTap(map: Map, point: Point) {
                            // ignore single tap
                        }

                        override fun onMapLongTap(map: Map, point: Point) {
                            selectedLocation = point
                            showAddMarkerDialog = true
                        }
                    })
                }
            },
            update = { view ->
                mapViewState.value = view

                val mapObjects = view.map.mapObjects
                mapObjects.clear()

                uiState.markers.forEach { marker ->
                    val point = Point(marker.latitude, marker.longitude)
                    val placemark = mapObjects.addPlacemark(point)
                    placemark.userData = marker

                    placemark.addTapListener(MapObjectTapListener { _, _ ->
                        viewModel.selectMarker(marker)
                        true
                    })
                }
            }
        )

        // Floating action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.getCurrentLocation() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Моё местоположение")
            }
            FloatingActionButton(
                onClick = {
                    mapViewState.value?.map?.cameraPosition?.target?.let { location ->
                        selectedLocation = location
                        showAddMarkerDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить метку")
            }
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
                title = markerTitle,
                note = markerNote,
                photoUri = selectedPhotoUri,
                onTitleChange = { markerTitle = it },
                onNoteChange = { markerNote = it },
                onPhotoSelect = { imagePickerLauncher.launch("image/*") },
                onDismiss = {
                    showAddMarkerDialog = false
                    markerTitle = ""
                    markerNote = ""
                    selectedPhotoUri = null
                    selectedLocation = null
                },
                onConfirm = { location, title, note, photoUri, isPrivate ->
                    if (location != null && photoUri != null) {
                        viewModel.addMarker(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            photoUri = photoUri.toString(),
                            title = title.ifEmpty { null },
                            note = note.ifEmpty { null },
                            isPrivate = isPrivate
                        )
                        showAddMarkerDialog = false
                        markerTitle = ""
                        markerNote = ""
                        markerIsPrivate = false
                        selectedPhotoUri = null
                        selectedLocation = null
                    }
                },
                hasSubscription = hasSubscription.value,
                isPrivate = markerIsPrivate,
                onIsPrivateChange = { markerIsPrivate = it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerDialog(
    location: Point?,
    title: String,
    note: String,
    photoUri: Uri?,
    hasSubscription: Boolean,
    isPrivate: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPhotoSelect: () -> Unit,
    onIsPrivateChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Point?, String, String, Uri?, Boolean) -> Unit
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
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Название (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                
                if (hasSubscription) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPrivate,
                            onCheckedChange = onIsPrivateChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Приватная метка (только для меня)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(location, title, note, photoUri, isPrivate) },
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
    var title by remember { mutableStateOf(marker.title ?: "") }
    var note by remember { mutableStateOf(marker.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(marker.title ?: "Детали метки") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Координаты: ${marker.latitude}, ${marker.longitude}")

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                    onClick = { onUpdate(marker.copy(title = title.ifEmpty { null }, note = note.ifEmpty { null })) }
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

