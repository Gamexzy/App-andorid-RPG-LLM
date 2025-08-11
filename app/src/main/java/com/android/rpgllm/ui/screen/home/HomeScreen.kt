package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
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
                        val titleText = when (pagerState.currentPage) {
                            0 -> "Aventuras"
                            1 -> "Criar"
                            2 -> "Configurações"
                            else -> "" // Should not happen
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
                        containerColor = Color(0xFF121212),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aventuras") },
                        label = { Text("Aventuras") },
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        colors = navigationBarItemColors()
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Criar") },
                        label = { Text("Criar") },
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        colors = navigationBarItemColors()
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                        label = { Text("Configurações") },
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
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
                        onNavigateToGame = { adventureName ->
                            rootNavController.navigate(AppRoutes.gameScreen(adventureName))
                        }
                    )
                    1 -> CreationScreen(
                        modifier = modifierWithTransform,
                        gameViewModel = gameViewModel,
                        onAdventureCreated = { adventureName ->
                            rootNavController.navigate(AppRoutes.gameScreen(adventureName)) {
                                popUpTo(rootNavController.graph.startDestinationId)
                            }
                        }
                    )
                    2 -> SettingsScreen(
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
    selectedIconColor = Color(0xFF00C853),
    unselectedIconColor = Color.Gray,
    selectedTextColor = Color(0xFF00C853),
    unselectedTextColor = Color.Gray,
    indicatorColor = Color(0xFF2A2A2A)
)

