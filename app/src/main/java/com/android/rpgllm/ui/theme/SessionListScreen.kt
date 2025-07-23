// app/src/main/java/com/android/rpgllm/ui/theme/SessionListScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    onNavigateToCreation: () -> Unit,
    onNavigateToGame: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Sagas") },
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Aqui ficará a lista de sessões.", color = Color.Gray)
            // Botão de teste para navegar para um jogo existente
            Button(onClick = { onNavigateToGame("koragon_o_dragao") }, modifier = Modifier.padding(top = 60.dp)) {
                Text("Teste: Ir para o jogo 'Koragon'")
            }
        }
    }
}
