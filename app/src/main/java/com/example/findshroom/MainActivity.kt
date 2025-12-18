package com.example.findshroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.findshroom.ui.navigation.NavGraph
import com.example.findshroom.ui.navigation.Screen
import com.example.findshroom.ui.theme.FIndShroomTheme
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIndShroomTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    
    val isLoggedIn = remember {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.getLong("current_user_id", -1L) != -1L
    }
    
    val startDestination = if (isLoggedIn) Screen.Recognition.route else Screen.Login.route
    
    LaunchedEffect(Unit) {
        if (!isLoggedIn && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            if (isLoggedIn && currentRoute != Screen.Login.route) {
                TopAppBar(
                    title = { Text("FindShroom") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(Icons.Default.Person, contentDescription = "Профиль")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isLoggedIn && currentRoute != Screen.Login.route && 
                currentRoute != Screen.Profile.route && 
                currentRoute != Screen.Subscription.route &&
                currentRoute != Screen.Diary.route &&
                currentRoute != Screen.Admin.route) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        label = { Text("Распознавание") },
                        selected = currentRoute == Screen.Recognition.route,
                        onClick = {
                            navController.navigate(Screen.Recognition.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Map, contentDescription = null) },
                        label = { Text("Карта") },
                        selected = currentRoute == Screen.Map.route,
                        onClick = {
                            navController.navigate(Screen.Map.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        label = { Text("Справочник") },
                        selected = currentRoute == Screen.Guide.route,
                        onClick = {
                            navController.navigate(Screen.Guide.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination
        )
    }
}