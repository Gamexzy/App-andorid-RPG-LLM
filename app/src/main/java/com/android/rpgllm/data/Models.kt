// app/src/main/java/com/android/rpgllm/data/Models.kt
package com.android.rpgllm.data

// Representa a informação de uma sessão na lista
data class SessionInfo(
    val session_name: String,
    val player_name: String,
    val world_concept: String
)

// Representa o estado da UI para a lista de sessões
data class SessionListUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// Representa o estado da UI para a tela de criação
data class CreationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// NOVO: Representa uma ferramenta de ação contextual que o jogador pode usar.
data class GameTool(
    val displayName: String,
    val command: String
)

// NOVO: Estado da UI para o menu de ferramentas.
data class ToolMenuUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val tools: List<GameTool> = emptyList() // A lista agora começa vazia
)
