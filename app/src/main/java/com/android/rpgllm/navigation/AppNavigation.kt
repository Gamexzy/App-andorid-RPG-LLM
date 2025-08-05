package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.screen.auth.AuthScreen
import com.android.rpgllm.ui.screen.game.GameScreen
import com.android.rpgllm.ui.screen.home.HomeScreen
import com.android.rpgllm.ui.screen.management.CharactersScreen
import com.android.rpgllm.ui.screen.management.UniversesScreen

object AppRoutes {
    const val HOME = "home"
    const val AUTH = "auth"
    const val GAME_SCREEN = "game_screen/{sessionName}"
    const val CHARACTERS_SCREEN = "characters_screen"
    const val UNIVERSES_SCREEN = "universes_screen"

    fun gameScreen(sessionName: String) = "game_screen/$sessionName"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
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

        // --- CORREÇÃO APLICADA AQUI ---
        // Removido o parâmetro `navController` que já não é necessário.
        composable(AppRoutes.CHARACTERS_SCREEN) {
            CharactersScreen()
        }

        composable(AppRoutes.UNIVERSES_SCREEN) {
            UniversesScreen()
        }
    }
}
