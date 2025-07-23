// app/src/main/java/com/android/rpgllm/data/models.kt
package com.android.rpgllm.data

import org.json.JSONObject

// Representa a informação de uma sessão na lista
data class SessionInfo(
    val session_name: String,
    val player_name: String
)

// Representa o estado da UI para a lista de sessões
data class SessionListUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null // CAMPO ADICIONADO
)

// Representa o estado da UI para a tela de criação
data class CreationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
