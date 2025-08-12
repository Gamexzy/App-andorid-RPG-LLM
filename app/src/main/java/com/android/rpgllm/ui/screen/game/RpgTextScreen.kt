package com.android.rpgllm.ui.screen.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.rpgllm.ui.screen.home.outlinedTextFieldColors

@Composable
fun RpgTextScreen(
    gameState: GameState,
    toolMenuState: ToolMenuUiState,
    contentPadding: PaddingValues,
    onSendAction: (String) -> Unit,
    onToolSelected: (GameTool) -> Unit
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(12.dp)
            ) {
                items(gameState.narrativeLines) { line ->
                    Text(
                        text = line,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge, // Usando o estilo do tema
                        modifier = Modifier.padding(vertical = 2.dp)

                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        OutlinedTextField(
            value = playerInput,
            onValueChange = { playerInput = it },
            label = { Text("Digite sua ação ou /tools...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // PARTE MODIFICADA: Usando a função de cores do tema
            colors = outlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendAction() }),
            enabled = !gameState.isLoading
        )
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
            // PARTE MODIFICADA
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.tools) { tool ->
                    Button(
                        onClick = { onToolSelected(tool) },
                        shape = RoundedCornerShape(50),
                        // PARTE MODIFICADA
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
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
