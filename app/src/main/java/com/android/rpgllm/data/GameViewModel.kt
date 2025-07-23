// app/src/main/java/com/android/rpgllm/data/GameViewModel.kt
package com.android.rpgllm.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.rpgllm.BuildConfig
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

// Enum movido para fora da classe para ser acessível por outras classes, como a MainActivity
enum class VersionStatus {
    CHECKING,
    UP_TO_DATE,
    OUTDATED,
    ERROR
}

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isEmulatorMode = MutableStateFlow(false)
    val isEmulatorMode: StateFlow<Boolean> = _isEmulatorMode.asStateFlow()

    private val _customIpAddress = MutableStateFlow("")
    val customIpAddress: StateFlow<String> = _customIpAddress.asStateFlow()

    private val _versionStatus = MutableStateFlow(VersionStatus.CHECKING)
    val versionStatus: StateFlow<VersionStatus> = _versionStatus.asStateFlow()

    private var currentSessionName: String? = null

    private val emulatorIp = "10.0.2.2"
    private val physicalDeviceIp = "192.168.0.104"

    init {
        checkAppVersion()
    }

    fun loadSession(sessionName: String) {
        currentSessionName = sessionName
        _gameState.update {
            GameState(narrativeLines = listOf("A carregar saga '$sessionName'..."))
        }
        fetchGameState()
    }

    fun checkAppVersion() {
        _versionStatus.value = VersionStatus.CHECKING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getCurrentBaseUrl()}/status")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                    val serverInfo = JSONObject(responseBody)
                    val minVersion = serverInfo.getString("minimum_client_version")

                    if (BuildConfig.VERSION_NAME.toDouble() >= minVersion.toDouble()) {
                        _versionStatus.value = VersionStatus.UP_TO_DATE
                    } else {
                        _versionStatus.value = VersionStatus.OUTDATED
                    }
                } else {
                    _versionStatus.value = VersionStatus.ERROR
                }
            } catch (e: Exception) {
                println("Erro de conexão ao verificar versão: ${e.message}")
                _versionStatus.value = VersionStatus.ERROR
            }
        }
    }

    fun toggleEmulatorMode() {
        _isEmulatorMode.value = !_isEmulatorMode.value
        checkAppVersion()
    }

    fun setCustomIpAddress(address: String) {
        _customIpAddress.value = address
        checkAppVersion()
    }

    private fun getCurrentBaseUrl(): String {
        val customIp = _customIpAddress.value.trim()
        if (customIp.isNotEmpty()) {
            return if (customIp.startsWith("http")) customIp else "http://$customIp"
        }
        val ip = if (_isEmulatorMode.value) emulatorIp else physicalDeviceIp
        return "http://$ip:5000"
    }

    fun fetchGameState() {
        val session = currentSessionName ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/state")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                    val parsedState = parseGameState(responseBody)
                    _gameState.update { currentState ->
                        // Mantém a narrativa atual ao atualizar o estado
                        parsedState.copy(narrativeLines = currentState.narrativeLines)
                    }
                } else {
                    _gameState.update { it.copy(narrativeLines = it.narrativeLines + "Erro ao carregar estado do jogo.") }
                }
            } catch (e: Exception) {
                _gameState.update { it.copy(narrativeLines = it.narrativeLines + "Erro de conexão ao carregar estado: ${e.message}") }
            }
        }
    }

    fun sendPlayerAction(action: String) {
        val session = currentSessionName ?: return

        _gameState.update { currentState ->
            val updatedLines = currentState.narrativeLines + "\n\nSua ação: $action" + "Mestre de Jogo a pensar..."
            currentState.copy(isLoading = true, narrativeLines = updatedLines)
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/execute_turn")
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
