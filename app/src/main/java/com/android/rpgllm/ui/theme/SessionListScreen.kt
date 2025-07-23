// app/src/main/java/com/android/rpgllm/ui/theme/SessionListScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.SessionInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    gameViewModel: GameViewModel = viewModel(),
    onNavigateToCreation: () -> Unit,
    onNavigateToGame: (String) -> Unit
) {
    val uiState by gameViewModel.sessionListState.collectAsState()

    // Busca as sessões quando o ecrã é exibido pela primeira vez
    LaunchedEffect(Unit) {
        gameViewModel.fetchSessions()
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreation,
                containerColor = Color(0xFF00C853)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Criar Nova Saga", tint = Color.White)
            }
        }
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
                // --- CORREÇÃO APLICADA AQUI ---
                uiState.errorMessage != null -> {
                    Text(
                        text = "Erro ao carregar sagas:\n${uiState.errorMessage}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                uiState.sessions.isEmpty() -> {
                    Text(
                        "Nenhuma saga encontrada.\nClique em '+' para criar uma nova aventura!",
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
                            SessionCard(session = session, onClick = { onNavigateToGame(session.session_name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: SessionInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
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
                text = "Sessão: ${session.session_name}",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
