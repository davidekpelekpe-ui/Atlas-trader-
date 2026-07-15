package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreenDesigned
import com.example.ui.screens.QrCodeScreen
import com.example.ui.screens.TradeAnalysisScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object QrCode : Screen("qr_code")
    object TradeAnalysis : Screen("trade_analysis")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreenDesigned(
                onShareClick = {
                    // Handle direct share
                },
                onQrCodeClick = {
                    navController.navigate(Screen.QrCode.route)
                },
                onSettingsClick = {
                    // Handle settings
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
        composable(Screen.TradeAnalysis.route) {
            TradeAnalysisScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
