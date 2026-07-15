package com.alarm.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarm.app.presentation.main.MainScreen
import com.alarm.app.presentation.qrlibrary.QrLibraryScreen

/**
 * Sets up the navigation host for the application.
 * Defines the composable destinations and the navigation paths between them.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.MAIN) {
        composable(AppDestinations.MAIN) {
            MainScreen(
                onNavigateToQrLibrary = { navController.navigate(AppDestinations.QR_LIBRARY) }
            )
        }
        composable(AppDestinations.QR_LIBRARY) {
            QrLibraryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
