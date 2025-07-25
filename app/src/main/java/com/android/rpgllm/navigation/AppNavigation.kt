// app/src/main/java/com/android/rpgllm/navigation/AppNavigation.kt
package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.theme.GameScreen // Renomeado para clareza
import com.android.rpgllm.ui.theme.HomeScreen

object AppRoutes {
    const val HOME = "home"
    const val GAME_SCREEN = "game_screen/{sessionName}"

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

        composable(AppRoutes.GAME_SCREEN) { backStackEntry ->
            val sessionName = backStackEntry.arguments?.getString("sessionName") ?: "error_session"
            GameScreen( // O antigo MainScreen agora é GameScreen
                gameViewModel = gameViewModel,
                sessionName = sessionName
            )
        }
    }
}
