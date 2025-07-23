// app/src/main/java/com/android/rpgllm/data/GameViewModel.kt
package com.android.rpgllm.data // PACOTE ATUALIZADO

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

// Enum para controlar o status da verificação de versão
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

    private val emulatorIp = "10.0.2.2"
    private val physicalDeviceIp = "192.168.0.104"

    init {
        _gameState.update {
            it.copy(narrativeLines = listOf("Bem-vindo ao universo! Para começar, diga-me seu nome ou descreva o que você gostaria de ser."))
        }
        checkAppVersion()
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

    fun sendPlayerAction(action: String) {
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

    fun fetchGameState() { /* ... */ }
    private fun parseGameState(jsonString: String): GameState { /* ... */ return GameState() }
}
