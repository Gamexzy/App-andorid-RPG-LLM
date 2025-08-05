package com.android.rpgllm.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.SessionInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventuresScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    onNavigateToGame: (String) -> Unit,
    onOpenMenu: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Aventuras") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        AdventureListContent(
            modifier = Modifier.padding(paddingValues),
            gameViewModel = gameViewModel,
            onNavigateToGame = onNavigateToGame
        )
    }
}

@Composable
private fun AdventureListContent(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    onNavigateToGame: (String) -> Unit
) {
    val uiState by gameViewModel.sessionListState.collectAsState()
    val authState by gameViewModel.authUiState.collectAsState() // Observa o estado de autenticação
    var contextMenuSession by remember { mutableStateOf<SessionInfo?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<SessionInfo?>(null) }

    // --- OTIMIZAÇÃO APLICADA AQUI ---
    // O efeito agora é acionado pelo estado de autenticação.
    // A busca por sessões só acontece DEPOIS que o login for confirmado.
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            gameViewModel.fetchSessions()
        }
    }

    showDeleteDialogFor?.let { session ->
        DeleteConfirmationDialog(
            sessionInfo = session,
            onConfirm = {
                gameViewModel.deleteSession(session.session_name)
                showDeleteDialogFor = null
            },
            onDismiss = {
                showDeleteDialogFor = null
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Mostra o loading se estiver autenticando OU carregando as sessões
            authState.isLoading || uiState.isLoading -> CircularProgressIndicator(color = Color(0xFF00C853))
            uiState.errorMessage != null -> Text(
                text = "Erro ao carregar aventuras:\n${uiState.errorMessage}",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            uiState.sessions.isEmpty() && !uiState.isLoading -> Text(
                "Nenhuma aventura encontrada.\nUse o separador 'Criar' para começar uma nova!",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.sessions) { session ->
                        SessionCard(
                            session = session,
                            onClick = { onNavigateToGame(session.session_name) },
                            onLongClick = { contextMenuSession = session },
                            contextMenu = {
                                AdventureContextMenu(
                                    expanded = contextMenuSession == session,
                                    onDismiss = { contextMenuSession = null },
                                    onDeleteClick = {
                                        showDeleteDialogFor = session
                                        contextMenuSession = null
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    session: SessionInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    contextMenu: @Composable () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = session.player_name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.world_concept,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }
        }
        contextMenu()
    }
}

@Composable
fun AdventureContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Excluir Aventura", color = Color.Red) },
            onClick = onDeleteClick,
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir Aventura",
                    tint = Color.Red
                )
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    sessionInfo: SessionInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Aventura") },
        text = { Text("Tem a certeza que quer excluir permanentemente a aventura de '${sessionInfo.player_name}'? Esta ação não pode ser desfeita.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
