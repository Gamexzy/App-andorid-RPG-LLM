// app/src/main/java/com/android/rpgllm/data/Models.kt
package com.android.rpgllm.data

import org.json.JSONObject

// --- Modelos para a Lista de Sessões e Criação ---

data class SessionInfo(
    val session_name: String,
    val player_name: String,
    val world_concept: String
)

data class SessionListUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class CreationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// --- Modelos para as Ferramentas Contextuais ---

data class GameTool(
    val displayName: String,
    val command: String
)

data class ToolMenuUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val tools: List<GameTool> = emptyList()
)

// --- Modelos para a Tela de Jogo ---

data class GameState(
    // Dados do Servidor
    val base: PlayerBase = PlayerBase(),
    val vitals: PlayerVitals = PlayerVitals(),
    val possessions: List<PlayerPossession> = emptyList(),

    // Estado da UI
    val narrativeLines: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class PlayerBase(
    val nome: String = "Aguardando...",
    val local_nome: String = "Desconhecido"
)

data class PlayerVitals(
    val fome: String = "-",
    val sede: String = "-",
    val cansaco: String = "-",
    val humor: String = "-"
)

data class PlayerPossession(
    val itemName: String = "Item desconhecido",
    val profile: JSONObject = JSONObject()
)

// --- NOVOS MODELOS PARA AUTENTICAÇÃO ---

/**
 * Representa o estado da UI para as telas de autenticação.
 */
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Representa o resultado de uma operação de autenticação (login/registo).
 * É uma sealed class para garantir que apenas os resultados definidos (Success, Error)
 * possam ser utilizados, tornando o código mais seguro e previsível.
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
