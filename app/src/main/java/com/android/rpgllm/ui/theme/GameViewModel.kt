// app/src/main/java/com/android/rpgllm/ui/theme/GameViewModel.kt
package com.android.rpgllm.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GameViewModel : ViewModel() {

    // --- Estado da UI ---
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // --- Estado do Modo de Conexão ---
    private val _isEmulatorMode = MutableStateFlow(false)
    val isEmulatorMode: StateFlow<Boolean> = _isEmulatorMode.asStateFlow()

    private val emulatorIp = "10.0.2.2"
    private val physicalDeviceIp = "192.168.0.104" // IP do seu PC

    init {
        // Adiciona a mensagem inicial ao carregar o ViewModel
        _gameState.update {
            it.copy(narrativeLines = listOf("Bem-vindo ao universo! Para começar, diga-me seu nome ou descreva o que você gostaria de ser."))
        }
    }

    fun toggleEmulatorMode() {
        _isEmulatorMode.value = !_isEmulatorMode.value
    }

    private fun getCurrentBaseUrl(): String {
        val ip = if (_isEmulatorMode.value) emulatorIp else physicalDeviceIp
        return "http://$ip:5000"
    }

    fun fetchGameState() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getCurrentBaseUrl()}/get_game_state")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                    val parsedState = parseGameState(responseBody)
                    _gameState.update { it.copy(
                        base = parsedState.base,
                        vitals = parsedState.vitals,
                        possessions = parsedState.possessions
                    ) }
                } else {
                    println("Erro do servidor ao buscar estado: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                println("Erro de conexão ao buscar estado: ${e.message}")
            }
        }
    }

    fun sendPlayerAction(action: String) {
        // Adiciona a ação do jogador e a mensagem de "pensando" à narrativa
        _gameState.update { currentState ->
            val updatedLines = currentState.narrativeLines + "\n\nSua ação: $action" + "Mestre de Jogo pensando..."
            currentState.copy(isLoading = true, narrativeLines = updatedLines)
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getCurrentBaseUrl()}/execute_turn")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 60000

                val jsonInputString = JSONObject().apply { put("player_action", action) }.toString()
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val newLines = if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                    val narrative = JSONObject(responseBody).optString("narrative", "Erro: 'narrative' não encontrada.")
                    listOf("\n\n$narrative")
                } else {
                    val errorStream = connection.errorStream?.let {
                        BufferedReader(InputStreamReader(it, "UTF-8")).use { r -> r.readText() }
                    } ?: "Nenhuma informação de erro."
                    listOf("\n\nErro do Servidor ($responseCode): $errorStream")
                }

                _gameState.update { currentState ->
                    // Remove a mensagem "pensando..." e adiciona a resposta
                    val currentLines = currentState.narrativeLines.dropLast(1)
                    currentState.copy(narrativeLines = currentLines + newLines)
                }

            } catch (e: Exception) {
                _gameState.update { currentState ->
                    val currentLines = currentState.narrativeLines.dropLast(1)
                    val errorLine = "\n\nErro de Conexão: Verifique o servidor e o IP. (${e.message})"
                    currentState.copy(narrativeLines = currentLines + errorLine)
                }
            } finally {
                _gameState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun parseGameState(jsonString: String): GameState {
        val json = JSONObject(jsonString)
        val baseJson = json.optJSONObject("base") ?: JSONObject()
        val playerBase = PlayerBase(
            nome = baseJson.optString("nome", "N/A"),
            local_nome = baseJson.optString("local_nome", "N/A")
        )

        val vitalsJson = json.optJSONObject("vitals") ?: JSONObject()
        val playerVitals = PlayerVitals(
            fome = vitalsJson.optString("fome", "-"),
            sede = vitalsJson.optString("sede", "-"),
            cansaco = vitalsJson.optString("cansaco", "-"),
            humor = vitalsJson.optString("humor", "-")
        )

        val possessionsJsonArray = json.optJSONArray("posses")
        val possessionsList = mutableListOf<PlayerPossession>()
        if (possessionsJsonArray != null) {
            for (i in 0 until possessionsJsonArray.length()) {
                val pJson = possessionsJsonArray.getJSONObject(i)
                possessionsList.add(
                    PlayerPossession(
                        itemName = pJson.optString("item_nome", "Item desconhecido"),
                        profile = JSONObject(pJson.optString("perfil_json", "{}"))
                    )
                )
            }
        }

        return GameState(base = playerBase, vitals = playerVitals, possessions = possessionsList)
    }
}
