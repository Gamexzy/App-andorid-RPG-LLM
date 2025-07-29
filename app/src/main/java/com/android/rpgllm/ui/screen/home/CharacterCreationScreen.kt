package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.VersionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    gameViewModel: GameViewModel,
    onSessionCreated: (String) -> Unit
) {
    var characterName by remember { mutableStateOf("") }
    var worldConcept by remember { mutableStateOf("") }
    val uiState by gameViewModel.creationState.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        gameViewModel.checkAppVersion()
    }

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
                colors = outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = worldConcept,
                onValueChange = { worldConcept = it },
                label = { Text("Conceito do Mundo (Ex: Fantasia sombria)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading || versionStatus == VersionStatus.CHECKING) {
                CircularProgressIndicator(color = Color(0xFF00C853))
            } else if (versionStatus != VersionStatus.UP_TO_DATE) {
                Text(
                    "Não é possível criar uma saga. Verifique a conexão com o servidor na aba 'Configurações'.",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
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
