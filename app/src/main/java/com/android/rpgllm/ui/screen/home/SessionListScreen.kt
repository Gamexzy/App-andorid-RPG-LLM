package com.android.rpgllm.ui.screen.home

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.SessionInfo
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    gameViewModel: GameViewModel,
    onNavigateToGame: (String) -> Unit
) {
    val uiState by gameViewModel.sessionListState.collectAsState()
    // Estado para controlar qual saga terá o menu de contexto aberto
    var contextMenuSession by remember { mutableStateOf<SessionInfo?>(null) }
    // Estado para controlar o diálogo de confirmação de exclusão
    var showDeleteDialogFor by remember { mutableStateOf<SessionInfo?>(null) }

    LaunchedEffect(Unit) {
        gameViewModel.fetchSessions()
    }


    // Diálogo de confirmação
    showDeleteDialogFor?.let { session ->
        DeleteConfirmationDialog(
            sessionInfo = session,
            onConfirm = {
                gameViewModel.deleteSession(session.session_name)
                showDeleteDialogFor = null // Fecha o diálogo
            },
            onDismiss = {
                showDeleteDialogFor = null // Fecha o diálogo
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
                uiState.isLoading -> CircularProgressIndicator(color = Color(0xFF00C853))
                uiState.errorMessage != null -> Text(
                    text = "Erro ao carregar sagas:\n${uiState.errorMessage}",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                uiState.sessions.isEmpty() -> Text(
                    "Nenhuma saga encontrada.\nUse o separador 'Criar' para começar uma nova aventura!",
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
                                    // O DropdownMenu é passado como um Composable para o SessionCard
                                    SagaContextMenu(
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
}

@Preview
@Composable
fun SessionListScreenPreview() {
    // ViewModel pode ser mockado ou uma instância real pode ser usada se não houver dependências complexas
    val gameViewModel: GameViewModel = viewModel() // Ou um mock
    SessionListScreen(
        gameViewModel = gameViewModel,
        onNavigateToGame = {}
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    session: SessionInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    contextMenu: @Composable () -> Unit // Parâmetro para receber o menu
) {
    // Usamos um Box para que o DropdownMenu possa ser ancorado corretamente ao Card
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
        // Renderiza o menu de contexto
        contextMenu()
    }
}

@Preview
@Composable
fun SessionCardPreview() {
    val session = SessionInfo(session_name = "Saga do Dragão", player_name = "Aragorn", world_concept = "Um mundo de fantasia medieval ameaçado por um dragão ancestral.")
    SessionCard(
        session = session,
        onClick = {},
        onLongClick = {},
        contextMenu = {
            // Pode-se passar um SagaContextMenuPreview aqui ou um Composable vazio
        }
    )
}
@Composable
fun SagaContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // Opção 1: Excluir
        DropdownMenuItem(
            text = { Text("Excluir Saga", color = Color.Red) },
            onClick = onDeleteClick,
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir Saga",
                    tint = Color.Red
                )
            }
        )
        // Opção 2: Renomear (Exemplo)
        DropdownMenuItem(
            text = { Text("Renomear") },
            onClick = {
                Toast.makeText(context, "Função 'Renomear' ainda não implementada.", Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.DriveFileRenameOutline,
                    contentDescription = "Renomear"
                )
            }
        )
        // Opção 3: Duplicar (Exemplo)
        DropdownMenuItem(
            text = { Text("Duplicar") },
            onClick = {
                Toast.makeText(context, "Função 'Duplicar' ainda não implementada.", Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Duplicar"
                )
            }
        )
    }
}

@Preview
@Composable
fun SagaContextMenuPreview() {
    SagaContextMenu(
        expanded = true,
        onDismiss = {},
        onDeleteClick = {}
    )
}

@Composable
fun DeleteConfirmationDialog(
    sessionInfo: SessionInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Saga") },
        text = { Text("Tem a certeza que quer excluir permanentemente a saga de '${sessionInfo.player_name}'? Esta ação não pode ser desfeita.") },
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

@Preview
@Composable
fun DeleteConfirmationDialogPreview() {
    val sessionInfo = SessionInfo(session_name = "Saga de Teste", player_name = "Jogador Teste", world_concept = "Mundo de teste")
    DeleteConfirmationDialog(
        sessionInfo = sessionInfo,
        onConfirm = {},
        onDismiss = {}
    )
}
