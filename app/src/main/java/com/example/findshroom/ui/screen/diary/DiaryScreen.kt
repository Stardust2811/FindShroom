package com.example.findshroom.ui.screen.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.findshroom.data.model.DiaryEntry
import com.example.findshroom.ui.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Личный дневник") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить запись")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.entries) { entry ->
                DiaryEntryCard(entry = entry)
            }
        }
        
        if (showAddDialog) {
            AddDiaryEntryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { note, mushrooms ->
                    viewModel.addEntry(note, mushrooms)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateFormat.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.note,
                style = MaterialTheme.typography.bodyLarge
            )
            if (entry.mushroomsCollected > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Собрано грибов: ${entry.mushroomsCollected}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiaryEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var mushrooms by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = mushrooms,
                    onValueChange = { if (it.all { char -> char.isDigit() }) mushrooms = it },
                    label = { Text("Количество грибов") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note, mushrooms.toIntOrNull() ?: 0) },
                enabled = note.isNotBlank()
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

