package com.example.findshroom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.findshroom.ui.screen.guide.GuideScreen
import com.example.findshroom.ui.screen.map.MapScreen
import com.example.findshroom.ui.screen.recognition.RecognitionScreen

sealed class Screen(val route: String) {
    object Recognition : Screen("recognition")
    object Map : Screen("map")
    object Guide : Screen("guide")
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Recognition.route,
        modifier = modifier
    ) {
        composable(Screen.Recognition.route) {
            RecognitionScreen()
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Guide.route) {
            GuideScreen()
        }
    }
}

