package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.screen.auth.AuthScreen
import com.android.rpgllm.ui.screen.game.GameScreen
import com.android.rpgllm.ui.screen.home.HomeScreen

object AppRoutes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val GAME_SCREEN = "game_screen/{sessionName}"
    fun gameScreen(sessionName: String) = "game_screen/$sessionName"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()
    val authState by gameViewModel.authUiState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated && navController.currentDestination?.route != AppRoutes.AUTH) {
            navController.navigate(AppRoutes.AUTH) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    val startDestination = if (authState.isAuthenticated) AppRoutes.HOME else AppRoutes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.AUTH) {
            AuthScreen(
                gameViewModel = gameViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                gameViewModel = gameViewModel,
                rootNavController = navController
            )
        }

        composable(AppRoutes.GAME_SCREEN) { backStackEntry ->
            val sessionName = backStackEntry.arguments?.getString("sessionName") ?: "error_session"
            GameScreen(
                gameViewModel = gameViewModel,
                sessionName = sessionName
            )
        }
    }
}
