package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CharacterTab(
    characterName: String,
    onCharacterNameChange: (String) -> Unit,
    characterClass: String,
    onCharacterClassChange: (String) -> Unit,
    characterBackstory: String,
    onCharacterBackstoryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = characterName,
            onValueChange = onCharacterNameChange,
            label = { Text("Nome do Personagem") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = outlinedTextFieldColors()
        )

        OutlinedTextField(
            value = characterClass,
            onValueChange = onCharacterClassChange,
            label = { Text("Classe (Ex: Bárbaro, Feiticeira)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = outlinedTextFieldColors()
        )

        OutlinedTextField(
            value = characterBackstory,
            onValueChange = onCharacterBackstoryChange,
            label = { Text("História do Personagem") },
            placeholder = { Text("Descreva o passado, aparência e personalidade do seu herói...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = outlinedTextFieldColors()
        )
    }
}
