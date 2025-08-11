package com.android.rpgllm.data

import org.json.JSONObject

// --- Modelos para a Lista de Sessões e Criação ---

data class AdventureInfo(
    val adventureName: String,
    val playerName: String,
    val worldConcept: String
)

data class AdventureListUiState(
    val adventures: List<AdventureInfo> = emptyList(),
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
    val base: PlayerBase = PlayerBase(),
    val vitals: PlayerVitals = PlayerVitals(),
    val possessions: List<PlayerPossession> = emptyList(),
    val narrativeLines: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class PlayerBase(
    val nome: String = "Aguardando...",
    val localNome: String = "Desconhecido"
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

// --- Modelos para Autenticação ---

data class AuthUiState(
    val isAuthenticated: Boolean = false, // Indica se há um token válido (seja anônimo ou real)
    val isAnonymous: Boolean = true,      // Distingue entre conta de convidado e conta registrada
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class AuthResult {
    // Retorna o tipo de usuário para o ViewModel saber como atualizar o estado
    data class Success(val isAnonymous: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
