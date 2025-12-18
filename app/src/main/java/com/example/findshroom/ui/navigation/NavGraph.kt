package com.example.findshroom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.findshroom.ui.screen.admin.AdminScreen
import com.example.findshroom.ui.screen.auth.LoginScreen
import com.example.findshroom.ui.screen.diary.DiaryScreen
import com.example.findshroom.ui.screen.guide.GuideScreen
import com.example.findshroom.ui.screen.map.MapScreen
import com.example.findshroom.ui.screen.profile.ProfileScreen
import com.example.findshroom.ui.screen.recognition.RecognitionScreen
import com.example.findshroom.ui.screen.subscription.SubscriptionScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Recognition : Screen("recognition")
    object Map : Screen("map")
    object Guide : Screen("guide")
    object Profile : Screen("profile")
    object Subscription : Screen("subscription")
    object Diary : Screen("diary")
    object Admin : Screen("admin")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Recognition.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Recognition.route) {
            RecognitionScreen()
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Guide.route) {
            GuideScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSubscription = {
                    navController.navigate(Screen.Subscription.route)
                },
                onNavigateToDiary = {
                    navController.navigate(Screen.Diary.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Diary.route) {
            DiaryScreen()
        }
        composable(Screen.Admin.route) {
            AdminScreen()
        }
    }
}

