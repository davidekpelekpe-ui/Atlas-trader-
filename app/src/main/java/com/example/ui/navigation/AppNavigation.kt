package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QrCodeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object QrCode : Screen("qr_code")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onShareClick = {
                    // Handle direct share
                },
                onQrCodeClick = {
                    navController.navigate(Screen.QrCode.route)
                }
            )
        }
        composable(Screen.QrCode.route) {
            QrCodeScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
