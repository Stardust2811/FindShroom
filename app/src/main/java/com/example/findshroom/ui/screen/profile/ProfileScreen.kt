package com.example.findshroom.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.findshroom.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSubscription: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Профиль") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = uiState.user?.username ?: "",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    uiState.stats?.let { stats ->
                        Text(
                            text = "Уровень: ${stats.level} - ${stats.getLevelTitle()}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Опыт: ${stats.experience}/${stats.getExperienceForNextLevel()}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Собрано грибов: ${stats.totalMushroomsCollected}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Создано меток: ${stats.totalMarkersCreated}")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.hasSubscription) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Премиум активна", style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNavigateToDiary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Book, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Личный дневник")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Премиум функции",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Приватные метки")
                        Text("• Личный дневник")
                        Text("• Система рейтинга")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSubscription,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Активировать подписку")
                        }
                    }
                }
            }
            
            if (uiState.isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToAdmin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Админ панель")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Выйти")
            }
        }
    }
}

