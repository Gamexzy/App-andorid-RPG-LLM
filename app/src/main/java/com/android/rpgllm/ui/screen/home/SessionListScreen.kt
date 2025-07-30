package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.SessionInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    gameViewModel: GameViewModel,
    onNavigateToGame: (String) -> Unit
) {
    val uiState by gameViewModel.sessionListState.collectAsState()
    // Estado para controlar qual saga está selecionada para exclusão
    var sessionToDelete by remember { mutableStateOf<SessionInfo?>(null) }

    LaunchedEffect(Unit) {
        gameViewModel.fetchSessions()
    }

    // Se uma saga foi selecionada, mostra o diálogo de confirmação
    sessionToDelete?.let { session ->
        DeleteConfirmationDialog(
            sessionInfo = session,
            onConfirm = {
                gameViewModel.deleteSession(session.session_name)
                sessionToDelete = null // Fecha o diálogo
            },
            onDismiss = {
                sessionToDelete = null // Fecha o diálogo
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("As Minhas Sagas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = Color(0xFF00C853))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = "Erro ao carregar sagas:\n${uiState.errorMessage}\n\nVá para Configurações para verificar a conexão.",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                uiState.sessions.isEmpty() -> {
                    Text(
                        "Nenhuma saga encontrada.\nUse o separador 'Criar' para começar uma nova aventura!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.sessions) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onNavigateToGame(session.session_name) },
                                // Define a saga a ser apagada ao pressionar e segurar
                                onLongClick = { sessionToDelete = session }
                            )
                        }
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
    onLongClick: () -> Unit // Novo callback para o clique longo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // `combinedClickable` permite ter um clique normal e um clique longo
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
}

@Composable
fun DeleteConfirmationDialog(
    sessionInfo: SessionInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apagar Saga") },
        text = { Text("Tem a certeza que quer apagar permanentemente a saga de '${sessionInfo.player_name}'?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Apagar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
