// app/src/main/java/com/android/rpgllm/ui/theme/CharacterCreationScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.rpgllm.data.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    gameViewModel: GameViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSessionCreated: (String) -> Unit
) {
    var characterName by remember { mutableStateOf("") }
    var worldConcept by remember { mutableStateOf("") }
    val uiState by gameViewModel.creationState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Nova Saga") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // --- CORREÇÃO APLICADA AQUI ---
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFF616161),
                    cursorColor = Color(0xFF00C853),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF303030),
                    unfocusedContainerColor = Color(0xFF303030),
                    focusedLabelColor = Color(0xFF00C853),
                    unfocusedLabelColor = Color(0xFF9E9E9E)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = worldConcept,
                onValueChange = { worldConcept = it },
                label = { Text("Conceito do Mundo (Ex: Fantasia sombria, cyberpunk, etc.)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                // --- CORREÇÃO APLICADA AQUI ---
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFF616161),
                    cursorColor = Color(0xFF00C853),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF303030),
                    unfocusedContainerColor = Color(0xFF303030),
                    focusedLabelColor = Color(0xFF00C853),
                    unfocusedLabelColor = Color(0xFF9E9E9E)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00C853))
            } else {
                uiState.errorMessage?.let {
                    Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = {
                        if (characterName.isNotBlank() && worldConcept.isNotBlank()) {
                            gameViewModel.createNewSession(characterName, worldConcept, onSessionCreated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = characterName.isNotBlank() && worldConcept.isNotBlank()
                ) {
                    Text("Iniciar Aventura")
                }
            }
        }
    }
}
