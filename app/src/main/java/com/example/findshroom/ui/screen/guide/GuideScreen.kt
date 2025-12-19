package com.example.findshroom.ui.screen.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.findshroom.data.model.Mushroom
import com.example.findshroom.ui.viewmodel.GuideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    viewModel: GuideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val error = uiState.error
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.searchMushrooms(it) },
            label = { Text("Поиск грибов") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Повторить")
                    }
                }
            }
        } else if (uiState.filteredMushrooms.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Справочник пуст. Сохраните грибы после распознавания, и они появятся здесь.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Mushrooms list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredMushrooms) { mushroom ->
                    MushroomCard(
                        mushroom = mushroom,
                        onClick = { viewModel.selectMushroom(mushroom) },
                        onDelete = { viewModel.deleteMushroom(mushroom) }
                    )
                }
            }
        }
    }
    
    // Mushroom details dialog
    uiState.selectedMushroom?.let { mushroom ->
        MushroomDetailsDialog(
            mushroom = mushroom,
            onDismiss = { viewModel.selectMushroom(null) },
            onEdit = { viewModel.setEditing(true) },
            onDelete = {
                viewModel.deleteMushroom(mushroom)
                viewModel.selectMushroom(null)
            }
        )
    }
    
    // Edit dialog
    if (uiState.isEditing && uiState.selectedMushroom != null) {
        EditMushroomDialog(
            mushroom = uiState.selectedMushroom!!,
            onDismiss = {
                viewModel.setEditing(false)
                viewModel.selectMushroom(null)
            },
            onSave = { updatedMushroom ->
                viewModel.saveMushroom(updatedMushroom)
            }
        )
    }
}

@Composable
fun MushroomCard(
    mushroom: Mushroom,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (mushroom.imageUri != null) {
                AsyncImage(
                    model = mushroom.imageUri,
                    contentDescription = mushroom.name,
                    modifier = Modifier
                        .size(80.dp)
                        .weight(0.3f)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = mushroom.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (mushroom.scientificName.isNotEmpty()) {
                    Text(
                        text = mushroom.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (mushroom.isEdible) "Съедобный" else "Несъедобный",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (mushroom.isEdible) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushroomDetailsDialog(
    mushroom: Mushroom,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mushroom.name) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mushroom.imageUri != null) {
                    AsyncImage(
                        model = mushroom.imageUri,
                        contentDescription = mushroom.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                
                if (mushroom.scientificName.isNotEmpty()) {
                    Text(
                        text = mushroom.scientificName,
                        style = MaterialTheme.typography.bodyMedium
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
                
                mushroom.characteristics?.let {
                    Text(
                        text = "Характеристики: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Редактировать")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMushroomDialog(
    mushroom: Mushroom,
    onDismiss: () -> Unit,
    onSave: (Mushroom) -> Unit
) {
    var name by remember { mutableStateOf(mushroom.name) }
    var scientificName by remember { mutableStateOf(mushroom.scientificName) }
    var description by remember { mutableStateOf(mushroom.description) }
    var isEdible by remember { mutableStateOf(mushroom.isEdible) }
    var habitat by remember { mutableStateOf(mushroom.habitat ?: "") }
    var season by remember { mutableStateOf(mushroom.season ?: "") }
    var characteristics by remember { mutableStateOf(mushroom.characteristics ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать гриб") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("Научное название") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEdible,
                        onCheckedChange = { isEdible = it }
                    )
                    Text("Съедобный")
                }
                
                OutlinedTextField(
                    value = habitat,
                    onValueChange = { habitat = it },
                    label = { Text("Место обитания") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Сезон") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = characteristics,
                    onValueChange = { characteristics = it },
                    label = { Text("Характеристики") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        mushroom.copy(
                            name = name,
                            scientificName = scientificName,
                            description = description,
                            isEdible = isEdible,
                            habitat = habitat.ifEmpty { null },
                            season = season.ifEmpty { null },
                            characteristics = characteristics.ifEmpty { null }
                        )
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

