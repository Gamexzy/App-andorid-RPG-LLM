package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.VersionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    onAdventureCreated: (String) -> Unit
) {
    var characterName by remember { mutableStateOf("") }
    var characterClass by remember { mutableStateOf("") }
    var characterBackstory by remember { mutableStateOf("") }
    var worldConcept by remember { mutableStateOf("") }

    val uiState by gameViewModel.creationState.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Personagem", "Universo")

    LaunchedEffect(Unit) {
        gameViewModel.checkAppVersion()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color(0xFF1F1F1F),
            contentColor = Color(0xFF00C853)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f)
        ) {
            when (tabIndex) {
                0 -> CharacterTab(
                    characterName = characterName,
                    onCharacterNameChange = { characterName = it },
                    characterClass = characterClass,
                    onCharacterClassChange = { characterClass = it },
                    characterBackstory = characterBackstory,
                    onCharacterBackstoryChange = { characterBackstory = it }
                )
                1 -> UniverseTab(
                    worldConcept = worldConcept,
                    onWorldConceptChange = { worldConcept = it }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        gameViewModel.createNewAdventure(
                            characterName = characterName,
                            characterClass = characterClass,
                            characterBackstory = characterBackstory,
                            worldConcept = worldConcept,
                            onAdventureCreated = onAdventureCreated
                        )
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
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
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
