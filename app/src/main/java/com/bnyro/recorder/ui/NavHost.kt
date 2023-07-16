package com.bnyro.recorder.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.screens.HomeScreen
import com.bnyro.recorder.ui.screens.PlayerScreen
import com.bnyro.recorder.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialRecorder: RecorderType
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(route = Destination.Home.route) {
            HomeScreen(initialRecorder, onNavigate = { destination ->
                navController.navigateTo(destination.route)
            })
        }

        composable(route = Destination.Settings.route) {
            SettingsScreen()
        }

        composable(route = Destination.RecordingPlayer.route) {
            PlayerScreen(showVideoModeInitially = false)
        }
    }
}

fun NavHostController.navigateTo(route: String) = this.navigate(route) {
    launchSingleTop = true
    restoreState = true
}
