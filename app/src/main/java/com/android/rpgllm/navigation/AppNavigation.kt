// app/src/main/java/com/android/rpgllm/navigation/AppNavigation.kt
package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.theme.CharacterCreationScreen
import com.android.rpgllm.ui.theme.MainScreen
import com.android.rpgllm.ui.theme.SessionListScreen

// Define as "rotas" (identificadores únicos) para cada tela
object AppRoutes {
    const val SESSION_LIST = "session_list"
    const val CHARACTER_CREATION = "character_creation"
    const val GAME_SCREEN = "game_screen/{sessionName}"

    fun gameScreen(sessionName: String) = "game_screen/$sessionName"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SESSION_LIST
    ) {
        composable(AppRoutes.SESSION_LIST) {
            SessionListScreen(
                gameViewModel = gameViewModel, // Passa o ViewModel
                onNavigateToCreation = { navController.navigate(AppRoutes.CHARACTER_CREATION) },
                onNavigateToGame = { sessionName -> navController.navigate(AppRoutes.gameScreen(sessionName)) }
            )
        }

        composable(AppRoutes.CHARACTER_CREATION) {
            CharacterCreationScreen(
                gameViewModel = gameViewModel, // Passa o ViewModel
                onNavigateBack = { navController.popBackStack() },
                onSessionCreated = { sessionName ->
                    navController.navigate(AppRoutes.gameScreen(sessionName)) {
                        popUpTo(AppRoutes.SESSION_LIST)
                    }
                }
            )
        }

        composable(AppRoutes.GAME_SCREEN) { backStackEntry ->
            val sessionName = backStackEntry.arguments?.getString("sessionName") ?: "error_session"
            MainScreen(
                gameViewModel = gameViewModel,
                sessionName = sessionName
            )
        }
    }
}
