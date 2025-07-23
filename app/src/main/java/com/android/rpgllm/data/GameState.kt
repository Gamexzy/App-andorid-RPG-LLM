// app/src/main/java/com/android/rpgllm/data/GameState.kt
package com.android.rpgllm.data // PACOTE ATUALIZADO

import org.json.JSONObject

// Representa a estrutura principal do JSON recebido do servidor e o estado da UI
data class GameState(
    // Dados do Servidor
    val base: PlayerBase = PlayerBase(),
    val vitals: PlayerVitals = PlayerVitals(),
    val possessions: List<PlayerPossession> = emptyList(),

    // Estado da UI
    val narrativeLines: List<String> = emptyList(),
    val isLoading: Boolean = false
)

// Representa a seção "base" dos dados do jogador
data class PlayerBase(
    val nome: String = "Aguardando...",
    val local_nome: String = "Desconhecido"
)

// Representa a seção "vitals" dos dados do jogador
data class PlayerVitals(
    val fome: String = "-",
    val sede: String = "-",
    val cansaco: String = "-",
    val humor: String = "-"
)

// Representa um item no inventário do jogador
data class PlayerPossession(
    val itemName: String = "Item desconhecido",
    val profile: JSONObject = JSONObject()
)
