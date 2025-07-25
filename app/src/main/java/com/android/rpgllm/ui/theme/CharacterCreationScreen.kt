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
    // O 'sessionName' foi removido, pois agora é gerado pelo servidor
    var characterName by remember { mutableStateOf("") }
    var worldConcept by remember { mutableStateOf("") }
    val uiState by gameViewModel.creationState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Nova Aventura") },
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
            // Campo para o Nome do Personagem (agora é o primeiro)
            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = getTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo para o Conceito do Mundo
            OutlinedTextField(
                value = worldConcept,
                onValueChange = { worldConcept = it },
                label = { Text("Conceito do Mundo e Personagem") },
                placeholder = { Text("Ex: Um western espacial onde sou um caçador de recompensas com um braço cibernético.")},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = getTextFieldColors()
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
                        // A chamada para o ViewModel agora só envia os dados essenciais
                        gameViewModel.createNewSession(characterName, worldConcept, onSessionCreated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    // A validação do botão também é simplificada
                    enabled = characterName.isNotBlank() && worldConcept.isNotBlank()
                ) {
                    Text("Iniciar Aventura")
                }
            }
        }
    }
}

@Composable
private fun getTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
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
}
