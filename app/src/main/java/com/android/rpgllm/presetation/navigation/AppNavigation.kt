// app/src/main/java/com/android/rpgllm/navigation/AppNavigation.kt
package com.android.rpgllm.presetation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.presetation.screens.game.GameViewModel
import com.android.rpgllm.presetation.theme.AuthScreen
import com.android.rpgllm.presetation.screens.game.GameScreen
import com.android.rpgllm.presetation.screens.home.HomeScreen

object AppRoutes {
    const val AUTH = "auth" // Tela de Login/Registro
    const val HOME = "home"
    const val GAME_SCREEN = "game_screen/{sessionName}"

    fun gameScreen(sessionName: String) = "game_screen/$sessionName"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()
    val authState by gameViewModel.authUiState.collectAsState()

    // Define a rota inicial baseada no estado de autenticação
    val startDestination = if (authState.isAuthenticated) AppRoutes.HOME else AppRoutes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.AUTH) {
            AuthScreen(
                gameViewModel = gameViewModel,
                onLoginSuccess = {
                    // Navega para a home e limpa a tela de auth da pilha
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                gameViewModel = gameViewModel,
                onNavigateToGame = { sessionName ->
                    navController.navigate(AppRoutes.gameScreen(sessionName))
                },
                onLogout = {
                    // Navega para a tela de auth e limpa todas as outras telas da pilha
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
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
