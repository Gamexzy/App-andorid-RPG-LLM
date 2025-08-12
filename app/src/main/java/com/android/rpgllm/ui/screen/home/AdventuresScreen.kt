package com.android.rpgllm.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rpgllm.data.AdventureInfo
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventuresScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    navController: NavController, // <-- PARÂMETRO ADICIONADO AQUI PARA CORRIGIR O ERRO
    onNavigateToGame: (String) -> Unit
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    // A tela agora usa um Scaffold para acomodar o FAB
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            CreationFabMenu(
                isExpanded = isFabMenuExpanded,
                onFabClick = { isFabMenuExpanded = !isFabMenuExpanded },
                onItemClick = { route ->
                    isFabMenuExpanded = false
                    navController.navigate(route)
                }
            )
        }
    ) { innerPadding ->
        AdventureListContent(
            // Aplicando o padding do Scaffold e o modifier da animação do Pager
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
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
    val uiState by gameViewModel.adventureListState.collectAsState()
    val authState by gameViewModel.authUiState.collectAsState()
    var contextMenuAdventure by remember { mutableStateOf<AdventureInfo?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<AdventureInfo?>(null) }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            gameViewModel.fetchAdventures()
        }
    }

    showDeleteDialogFor?.let { adventure ->
        DeleteConfirmationDialog(
            adventureInfo = adventure,
            onConfirm = {
                gameViewModel.deleteAdventure(adventure.adventureName)
                showDeleteDialogFor = null
            },
            onDismiss = {
                showDeleteDialogFor = null
            }
        )
    }

    Box(
        // PARTE MODIFICADA
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when {
            // PARTE MODIFICADA
            authState.isLoading || uiState.isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            uiState.errorMessage != null -> Text(
                text = "Erro ao carregar aventuras:\n${uiState.errorMessage}",
                // PARTE MODIFICADA
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            uiState.adventures.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(
                        "Nenhuma aventura encontrada.\nToque no botão '+' para começar uma nova!",
                        // PARTE MODIFICADA
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.adventures) { adventure ->
                        AdventureCard(
                            adventure = adventure,
                            onClick = { onNavigateToGame(adventure.adventureName) },
                            onLongClick = { contextMenuAdventure = adventure },
                            contextMenu = {
                                AdventureContextMenu(
                                    expanded = contextMenuAdventure == adventure,
                                    onDismiss = { contextMenuAdventure = null },
                                    onDeleteClick = {
                                        showDeleteDialogFor = adventure
                                        contextMenuAdventure = null
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

@Composable
fun CreationFabMenu(
    isExpanded: Boolean,
    onFabClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = isExpanded) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FabMenuItem(
                    text = "Aventura",
                    icon = Icons.Default.AutoStories,
                    onClick = { onItemClick(AppRoutes.ADVENTURE_CREATION_SCREEN) }
                )
                FabMenuItem(
                    text = "Universo",
                    icon = Icons.Default.Public,
                    onClick = { onItemClick(AppRoutes.UNIVERSE_CREATION_SCREEN) }
                )
                FabMenuItem(
                    text = "Personagem",
                    icon = Icons.Default.Person,
                    onClick = { onItemClick(AppRoutes.CHARACTER_CREATION_SCREEN) }
                )
            }
        }
        FloatingActionButton(
            onClick = onFabClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(
                if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Criar"
            )
        }
    }
}

@Composable
fun FabMenuItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Icon(icon, contentDescription = text)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdventureCard(
    adventure: AdventureInfo,
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
            // PARTE MODIFICADA
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = adventure.playerName,
                    // PARTE MODIFICADA
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = adventure.worldConcept,
                    // PARTE MODIFICADA
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            // PARTE MODIFICADA
            text = { Text("Excluir Aventura", color = MaterialTheme.colorScheme.error) },
            onClick = onDeleteClick,
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir Aventura",
                    // PARTE MODIFICADA
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    adventureInfo: AdventureInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Aventura") },
        text = { Text("Tem a certeza que quer excluir permanentemente a aventura de '${adventureInfo.playerName}'? Esta ação não pode ser desfeita.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                // PARTE MODIFICADA
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
