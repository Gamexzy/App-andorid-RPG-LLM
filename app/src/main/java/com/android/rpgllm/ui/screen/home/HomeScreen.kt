package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.navigation.AppRoutes

object HomeRoutes {
    const val SESSION_LIST = "home_session_list"
    const val CHARACTER_CREATION = "home_character_creation"
    const val SETTINGS = "home_settings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    gameViewModel: GameViewModel,
    rootNavController: NavController // O controlador de navegação principal
) {
    val homeNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E1E)
            ) {
                val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Sagas") },
                    label = { Text("Sagas") },
                    selected = currentRoute == HomeRoutes.SESSION_LIST,
                    onClick = { homeNavController.navigate(HomeRoutes.SESSION_LIST) },
                    colors = navigationBarItemColors()
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Criar") },
                    label = { Text("Criar") },
                    selected = currentRoute == HomeRoutes.CHARACTER_CREATION,
                    onClick = { homeNavController.navigate(HomeRoutes.CHARACTER_CREATION) },
                    colors = navigationBarItemColors()
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                    label = { Text("Configurações") },
                    selected = currentRoute == HomeRoutes.SETTINGS,
                    onClick = { homeNavController.navigate(HomeRoutes.SETTINGS) },
                    colors = navigationBarItemColors()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = homeNavController,
            startDestination = HomeRoutes.SESSION_LIST,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeRoutes.SESSION_LIST) {
                SessionListScreen(
                    gameViewModel = gameViewModel,
                    onNavigateToGame = { sessionName ->
                        rootNavController.navigate(AppRoutes.gameScreen(sessionName))
                    }
                )
            }
            composable(HomeRoutes.CHARACTER_CREATION) {
                CharacterCreationScreen(
                    gameViewModel = gameViewModel,
                    onSessionCreated = { sessionName ->
                        rootNavController.navigate(AppRoutes.gameScreen(sessionName)) {
                            popUpTo(rootNavController.graph.startDestinationId)
                        }
                    }
                )
            }
            composable(HomeRoutes.SETTINGS) {
                // --- CORREÇÃO APLICADA AQUI ---
                // Passamos o `rootNavController` para a SettingsScreen,
                // pois ela precisa navegar para uma rota principal (AUTH).
                SettingsScreen(
                    gameViewModel = gameViewModel,
                    navController = rootNavController
                )
            }
        }
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color(0xFF00C853),
    unselectedIconColor = Color.Gray,
    selectedTextColor = Color(0xFF00C853),
    unselectedTextColor = Color.Gray,
    indicatorColor = Color(0xFF2A2A2A)
)
