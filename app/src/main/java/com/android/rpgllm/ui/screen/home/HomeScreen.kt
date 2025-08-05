package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.navigation.AppRoutes
import com.android.rpgllm.ui.screen.management.MenuScreen
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    gameViewModel: GameViewModel,
    rootNavController: NavController
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E1E)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aventuras") },
                    label = { Text("Aventuras") },
                    selected = pagerState.currentPage <= 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    colors = navigationBarItemColors()
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Criar") },
                    label = { Text("Criar") },
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    colors = navigationBarItemColors()
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                    label = { Text("Configurações") },
                    selected = pagerState.currentPage == 3,
                    onClick = { scope.launch { pagerState.animateScrollToPage(3) } },
                    colors = navigationBarItemColors()
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            // --- ANIMAÇÃO APLICADA AQUI ---
            // Usamos o graphicsLayer para aplicar transformações visuais a cada página.
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )

            val scale = lerp(
                start = 0.85f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )

            val modifierWithTransform = Modifier.graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }

            // Aplicamos o modificador à página correspondente
            when (page) {
                0 -> MenuScreen(
                    modifier = modifierWithTransform,
                    navController = rootNavController,
                    onCloseMenu = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> AdventuresScreen(
                    modifier = modifierWithTransform,
                    gameViewModel = gameViewModel,
                    onNavigateToGame = { sessionName ->
                        rootNavController.navigate(AppRoutes.gameScreen(sessionName))
                    },
                    onOpenMenu = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
                2 -> CreationScreen(
                    modifier = modifierWithTransform,
                    gameViewModel = gameViewModel,
                    onSessionCreated = { sessionName ->
                        rootNavController.navigate(AppRoutes.gameScreen(sessionName)) {
                            popUpTo(rootNavController.graph.startDestinationId)
                        }
                    }
                )
                3 -> SettingsScreen(
                    modifier = modifierWithTransform,
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
