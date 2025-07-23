// app/src/main/java/com/android/rpgllm/ui/theme/MainScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.rpgllm.data.GameViewModel // IMPORT ATUALIZADO
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(gameViewModel: GameViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gameState by gameViewModel.gameState.collectAsState()
    val isEmulatorMode by gameViewModel.isEmulatorMode.collectAsState()
    val customIpAddress by gameViewModel.customIpAddress.collectAsState()

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            gameViewModel.fetchGameState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                gameState = gameState,
                isEmulatorMode = isEmulatorMode,
                onToggleEmulatorMode = { gameViewModel.toggleEmulatorMode() },
                customIpAddress = customIpAddress,
                onCustomIpChange = { newIp -> gameViewModel.setCustomIpAddress(newIp) }
            )
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
