package com.android.rpgllm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.ui.theme.AuthScreen
import com.android.rpgllm.ui.theme.GameScreen
import com.android.rpgllm.ui.theme.HomeScreen

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

    // Este efeito observa o estado de autenticação.
    // Se o utilizador fizer logout (isAuthenticated torna-se falso),
    // ele navegará para a tela de autenticação e limpará a pilha de navegação.
    LaunchedEffect(authState.isAuthenticated) {
        // Apenas navega se o destino atual não for já a tela de autenticação
        if (!authState.isAuthenticated && navController.currentDestination?.route != AppRoutes.AUTH) {
            navController.navigate(AppRoutes.AUTH) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

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
            // A chamada para HomeScreen está corrigida.
            // Agora passamos o navController principal para que ele possa
            // navegar para a GameScreen a partir das suas telas internas.
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
