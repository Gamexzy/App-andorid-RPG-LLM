// app/src/main/java/com/android/rpgllm/ui/theme/RpgTextScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpgTextScreen(
    gameState: GameState,
    contentPadding: PaddingValues,
    onSendAction: (String) -> Unit
) {
    var playerInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Efeito para rolar para o final da narrativa sempre que ela é atualizada
    LaunchedEffect(gameState.narrativeLines.size) {
        if (gameState.narrativeLines.isNotEmpty()) {
            listState.animateScrollToItem(gameState.narrativeLines.lastIndex)
        }
    }

    val sendAction = {
        val currentInput = playerInput.trim()
        if (currentInput.isNotEmpty() && !gameState.isLoading) {
            onSendAction(currentInput)
            playerInput = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF212121)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(12.dp)
            ) {
                items(gameState.narrativeLines) { line ->
                    Text(
                        text = line,
                        color = Color(0xFFE0E0E0),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).padding(bottom = 8.dp),
                color = Color(0xFF00C853),
                trackColor = Color(0xFF303030)
            )
        }

        OutlinedTextField(
            value = playerInput,
            onValueChange = { playerInput = it },
            label = { Text("Digite sua ação aqui...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendAction() }),
            enabled = !gameState.isLoading
        )

        Button(
            onClick = sendAction,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !gameState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = if (gameState.isLoading) "Enviando..." else "Enviar Ação",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
