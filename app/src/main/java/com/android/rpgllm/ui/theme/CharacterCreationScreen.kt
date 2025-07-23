// app/src/main/java/com/android/rpgllm/ui/theme/CharacterCreationScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
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
                // Adicionar botão de voltar se necessário
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                OutlinedTextField(
                    value = characterName,
                    onValueChange = { characterName = it },
                    label = { Text("Nome do Personagem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF00C853),
                        focusedBorderColor = Color(0xFF00C853),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00C853),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = worldConcept,
                    onValueChange = { worldConcept = it },
                    label = { Text("Conceito do Mundo") },
                    placeholder = { Text("Ex: Fantasia sombria, cyberpunk, space opera...")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        cursorColor = Color(0xFF00C853),
                        focusedBorderColor = Color(0xFF00C853),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00C853),
                        unfocusedLabelColor = Color.Gray,
                        placeholderColor = Color.Gray
                    )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF00C853))
                } else {
                    uiState.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
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
                        enabled = characterName.isNotBlank() && worldConcept.isNotBlank() && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text("Iniciar Aventura", color = Color.White)
                    }
                }
            }
        }
    }
}
