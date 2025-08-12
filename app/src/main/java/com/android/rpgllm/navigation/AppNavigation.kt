package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.screen.auth.AuthScreen
import com.android.rpgllm.ui.screen.game.GameScreen
import com.android.rpgllm.ui.screen.home.AdventureCreationScreen
import com.android.rpgllm.ui.screen.home.CharacterCreationScreen
import com.android.rpgllm.ui.screen.home.HomeScreen
import com.android.rpgllm.ui.screen.home.UniverseCreationScreen
import com.android.rpgllm.ui.screen.home.CharactersScreen
import com.android.rpgllm.ui.screen.home.UniversesScreen

object AppRoutes {
    const val HOME = "home"
    const val AUTH = "auth"
    const val GAME_SCREEN = "game_screen/{adventureName}"

    // Rotas de gerenciamento (existentes)
    const val CHARACTERS_SCREEN = "characters_screen"
    const val UNIVERSES_SCREEN = "universes_screen"

    // Novas rotas para as telas de criação
    const val CHARACTER_CREATION_SCREEN = "character_creation_screen"
    const val UNIVERSE_CREATION_SCREEN = "universe_creation_screen"
    const val ADVENTURE_CREATION_SCREEN = "adventure_creation_screen"


    fun gameScreen(adventureName: String) = "game_screen/$adventureName"
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
            val adventureName = backStackEntry.arguments?.getString("adventureName") ?: "error_adventure"
            GameScreen(
                gameViewModel = gameViewModel,
                adventureName = adventureName
            )
        }

        composable(AppRoutes.CHARACTERS_SCREEN) {
            CharactersScreen()
        }

        composable(AppRoutes.UNIVERSES_SCREEN) {
            UniversesScreen()
        }

        // Adicionando as novas telas de criação ao grafo de navegação
        composable(AppRoutes.CHARACTER_CREATION_SCREEN) {
            CharacterCreationScreen(
                gameViewModel = gameViewModel,
                navController = navController
            )
        }

        composable(AppRoutes.UNIVERSE_CREATION_SCREEN) {
            UniverseCreationScreen(
                gameViewModel = gameViewModel,
                navController = navController
            )
        }

        composable(AppRoutes.ADVENTURE_CREATION_SCREEN) {
            AdventureCreationScreen(
                gameViewModel = gameViewModel,
                navController = navController
            )
        }
    }
}
