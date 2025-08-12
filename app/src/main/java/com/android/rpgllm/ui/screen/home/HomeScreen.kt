package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.navigation.AppRoutes
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    gameViewModel: GameViewModel,
    rootNavController: NavController
) {
    // Page count reduzido para 2
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = pagerState.currentPage == 0,
        drawerContent = {
            AdventuresDrawerContent(
                navController = rootNavController,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Título ajustado para apenas 2 páginas
                        val titleText = when (pagerState.currentPage) {
                            0 -> "Aventuras"
                            1 -> "Configurações"
                            else -> ""
                        }
                        Text(titleText)
                    },
                    navigationIcon = {
                        if (pagerState.currentPage == 0) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    // PARTE MODIFICADA
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aventuras") },
                        label = { Text("Aventuras") },
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        colors = navigationBarItemColors()
                    )
                    // Item "Criar" removido
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                        label = { Text("Configurações") },
                        selected = pagerState.currentPage == 1, // Index ajustado
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } }, // Index ajustado
                        colors = navigationBarItemColors()
                    )
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding)
            ) { page ->
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

                when (page) {
                    0 -> AdventuresScreen(
                        modifier = modifierWithTransform,
                        gameViewModel = gameViewModel,
                        navController = rootNavController, // Passando o NavController
                        onNavigateToGame = { adventureName ->
                            rootNavController.navigate(AppRoutes.gameScreen(adventureName))
                        }
                    )
                    // Case da CreationScreen removido
                    1 -> SettingsScreen( // Index ajustado
                        modifier = modifierWithTransform,
                        gameViewModel = gameViewModel,
                        navController = rootNavController
                    )
                }
            }
        }
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
)
