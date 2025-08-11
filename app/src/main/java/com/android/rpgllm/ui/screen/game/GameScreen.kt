package com.android.rpgllm.ui.screen.game

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.android.rpgllm.data.GameViewModel
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    adventureName: String
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gameState by gameViewModel.gameState.collectAsState()
    val toolMenuState by gameViewModel.toolMenuState.collectAsState()

    LaunchedEffect(key1 = adventureName) {
        gameViewModel.loadAdventure(adventureName)
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            gameViewModel.fetchGameState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(gameState = gameState)
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Universo Emergente") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply { if (isClosed) open() else close() }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, "Abrir menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212),
                        titleContentColor = Color(0xFF00C853),
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            RpgTextScreen(
                gameState = gameState,
                toolMenuState = toolMenuState,
                contentPadding = innerPadding,
                onSendAction = { action -> gameViewModel.processPlayerInput(action) },
                onToolSelected = { tool -> gameViewModel.onToolSelected(tool) }
            )
        }
    }
}
