package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UniverseTab(
    worldConcept: String,
    onWorldConceptChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = worldConcept,
            onValueChange = onWorldConceptChange,
            label = { Text("Conceito do Universo") },
            placeholder = { Text("Descreva o cenário da sua aventura. Ex: Um reino de fantasia sombria onde a magia está morrendo...") },
            modifier = Modifier.fillMaxSize(),
            colors = outlinedTextFieldColors()
        )
    }
}
