package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.screen.auth.AuthScreen
import com.android.rpgllm.ui.screen.game.GameScreen
import com.android.rpgllm.ui.screen.home.HomeScreen

object AppRoutes {
    const val HOME = "home"
    const val AUTH = "auth" // A tela de login agora é um destino normal
    const val GAME_SCREEN = "game_screen/{sessionName}"
    fun gameScreen(sessionName: String) = "game_screen/$sessionName"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME // O app sempre começa aqui
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                gameViewModel = gameViewModel,
                rootNavController = navController
            )
        }

        composable(AppRoutes.AUTH) {
            AuthScreen(
                gameViewModel = gameViewModel,
                onLoginSuccess = {
                    // Após o login, volta para a tela anterior (Configurações)
                    navController.popBackStack()
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
