// app/src/main/java/com/android/rpgllm/ui/theme/RpgTextScreen.kt
package com.android.rpgllm.presetation.screens.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameState
import com.android.rpgllm.data.GameTool
import com.android.rpgllm.data.ToolMenuUiState

@Composable
fun RpgTextScreen(
    gameState: GameState,
    toolMenuState: ToolMenuUiState, // NOVO
    contentPadding: PaddingValues,
    onSendAction: (String) -> Unit,
    onToolSelected: (GameTool) -> Unit // NOVO
) {
    var playerInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(gameState.narrativeLines.size) {
        if (gameState.narrativeLines.isNotEmpty()) {
            listState.animateScrollToItem(gameState.narrativeLines.lastIndex)
        }
    }

    val sendAction = {
        if (playerInput.isNotBlank() && !gameState.isLoading) {
            onSendAction(playerInput)
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
        // Área da Narrativa
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF212121)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(12.dp)
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

        // NOVO: Seção do Menu de Ferramentas
        AnimatedVisibility(
            visible = toolMenuState.isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            ToolMenu(
                uiState = toolMenuState,
                onToolSelected = onToolSelected,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (gameState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).padding(bottom = 8.dp),
                color = Color(0xFF00C853),
                trackColor = Color(0xFF303030)
            )
        }

        // Área de Input
        OutlinedTextField(
            value = playerInput,
            onValueChange = { playerInput = it },
            label = { Text("Digite sua ação ou /tools...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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

        // Botão de Enviar (opcional, já que o teclado tem o botão de enviar)
        // Button( ... )
    }
}

@Composable
fun ToolMenu(
    uiState: ToolMenuUiState,
    onToolSelected: (GameTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color(0xFF00C853))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.tools) { tool ->
                    Button(
                        onClick = { onToolSelected(tool) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tool.displayName, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
