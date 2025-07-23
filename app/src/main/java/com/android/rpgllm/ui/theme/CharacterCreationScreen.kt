// app/src/main/java/com/android/rpgllm/ui/theme/CharacterCreationScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    onNavigateBack: () -> Unit,
    onSessionCreated: (String) -> Unit
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Aqui ficará o formulário de criação.", color = Color.Gray)
            // Botão de teste para simular a criação
            Button(onClick = { onSessionCreated("nova_saga_teste") }) {
                Text("Teste: Criar e ir para o Jogo")
            }
        }
    }
}
