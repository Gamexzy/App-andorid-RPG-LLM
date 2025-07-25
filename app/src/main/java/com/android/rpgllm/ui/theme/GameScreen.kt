// app/src/main/java/com/android/rpgllm/ui/theme/GameScreen.kt
// Renomeando o ficheiro MainScreen.kt para GameScreen.kt para melhor clareza
package com.android.rpgllm.ui.theme

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
    sessionName: String
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(key1 = sessionName) {
        gameViewModel.loadSession(sessionName)
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            gameViewModel.fetchGameState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(gameState = gameState) // As configurações foram removidas daqui
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
                contentPadding = innerPadding,
                onSendAction = { action -> gameViewModel.sendPlayerAction(action) }
            )
        }
    }
}
