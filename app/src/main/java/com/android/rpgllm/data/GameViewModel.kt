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
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class VersionStatus { NONE, CHECKING, UP_TO_DATE, OUTDATED, ERROR }

class GameViewModel : ViewModel() {

    // --- Estados da UI ---
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _sessionListState = MutableStateFlow(SessionListUiState())
    val sessionListState: StateFlow<SessionListUiState> = _sessionListState.asStateFlow()

    private val _creationState = MutableStateFlow(CreationUiState())
    val creationState: StateFlow<CreationUiState> = _creationState.asStateFlow()

    private val _versionStatus = MutableStateFlow(VersionStatus.NONE)
    val versionStatus: StateFlow<VersionStatus> = _versionStatus.asStateFlow()

    // --- Configurações de Conexão ---
    private val _isEmulatorMode = MutableStateFlow(false)
    val isEmulatorMode: StateFlow<Boolean> = _isEmulatorMode.asStateFlow()

    private val _customIpAddress = MutableStateFlow("")
    val customIpAddress: StateFlow<String> = _customIpAddress.asStateFlow()

    private var currentSessionName: String? = null
    private val emulatorIp = "10.0.2.2"
    private val physicalDeviceIp = "192.168.0.104" // IP ATUALIZADO

    // --- LÓGICA DE NAVEGAÇÃO E SESSÃO ---

    fun loadSession(sessionName: String) {
        currentSessionName = sessionName
        // Limpa o estado anterior para mostrar a mensagem de carregamento
        _gameState.update { GameState(narrativeLines = listOf("A carregar saga '$sessionName'...")) }
        fetchGameState(isInitialLoad = true)
    }

    fun fetchSessions() {
        _sessionListState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val canProceed = checkAppVersion()
            if (canProceed) {
                try {
                    val url = URL("${getCurrentBaseUrl()}/sessions")
                    val response = makeRequest(url, "GET")
                    if (response != null) {
                        val sessions = parseSessionList(response)
                        _sessionListState.update { it.copy(sessions = sessions, isLoading = false) }
                    } else {
                        _sessionListState.update { it.copy(isLoading = false, errorMessage = "Nenhuma resposta do servidor ao buscar sessões.") }
                    }
                } catch (e: Exception) {
                    _sessionListState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            } else {
                _sessionListState.update { it.copy(isLoading = false, errorMessage = "Falha na conexão com o servidor.") }
            }
        }
    }

    fun createNewSession(characterName: String, worldConcept: String, onSessionCreated: (String) -> Unit) {
        _creationState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val url = URL("${getCurrentBaseUrl()}/sessions/create")
                val payload = JSONObject().apply {
                    put("character_name", characterName)
                    put("world_concept", worldConcept)
                }
                val response = makeRequest(url, "POST", payload.toString())
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    val newSessionName = jsonResponse.getString("session_name")
                    val initialNarrative = jsonResponse.getString("initial_narrative")

                    _gameState.value = GameState(narrativeLines = listOf(initialNarrative))
                    onSessionCreated(newSessionName)
                } else {
                    _creationState.update { it.copy(isLoading = false, errorMessage = "Falha ao criar sessão. O servidor respondeu com um erro.") }
                }
            } catch (e: Exception) {
                _creationState.update { it.copy(isLoading = false, errorMessage = "Erro de conexão: ${e.message}") }
            } finally {
                _creationState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- LÓGICA DE REDE ---

    suspend fun checkAppVersion(): Boolean {
        _versionStatus.value = VersionStatus.CHECKING
        return try {
            val url = URL("${getCurrentBaseUrl()}/status")
            val response = makeRequest(url, "GET")
            if (response != null) {
                val serverInfo = JSONObject(response)
                val minVersion = serverInfo.getString("minimum_client_version")
                if (BuildConfig.VERSION_NAME.toDouble() >= minVersion.toDouble()) {
                    _versionStatus.value = VersionStatus.UP_TO_DATE
                    true
                } else {
                    _versionStatus.value = VersionStatus.OUTDATED
                    false
                }
            } else {
                _versionStatus.value = VersionStatus.ERROR
                false
            }
        } catch (_: Exception) {
            _versionStatus.value = VersionStatus.ERROR
            false
        }
    }

    fun toggleEmulatorMode() {
        _isEmulatorMode.value = !_isEmulatorMode.value
    }

    fun setCustomIpAddress(address: String) {
        _customIpAddress.value = address
    }

    private fun getCurrentBaseUrl(): String {
        val customIp = _customIpAddress.value.trim()
        val finalIp = when {
            customIp.isNotEmpty() -> customIp.removePrefix("http://").removePrefix("https://").split(":").first()
            _isEmulatorMode.value -> emulatorIp
            else -> physicalDeviceIp
        }
        return "http://$finalIp:5000"
    }

    fun sendPlayerAction(action: String) {
        val session = currentSessionName ?: return

        // Adiciona a ação do jogador à UI imediatamente
        _gameState.update { currentState ->
            val updatedLines = currentState.narrativeLines + "\n\n> $action"
            currentState.copy(isLoading = true, narrativeLines = updatedLines)
        }

        viewModelScope.launch {
            // --- CORREÇÃO APLICADA AQUI ---
            // A variável 'newLines' é agora uma 'val' e recebe o resultado do bloco 'try-catch',
            // garantindo que ela seja sempre inicializada.
            val newLines: List<String> = try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/execute_turn")
                val payload = JSONObject().apply { put("player_action", action) }
                val response = makeRequest(url, "POST", payload.toString())

                if (response != null) {
                    val narrative = JSONObject(response).optString("narrative", "Erro: 'narrative' não encontrada.")
                    listOf("\n\n$narrative")
                } else {
                    listOf("\n\nErro do Servidor: Nenhuma resposta recebida.")
                }
            } catch (e: Exception) {
                listOf("\n\nErro de Conexão: Verifique o servidor e o IP. (${e.message})")
            }

            // Atualiza o estado da UI com a resposta do servidor
            _gameState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    narrativeLines = currentState.narrativeLines + newLines
                )
            }
        }
    }

    fun fetchGameState(isInitialLoad: Boolean = false) {
        val session = currentSessionName ?: return
        viewModelScope.launch {
            try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/state")
                val response = makeRequest(url, "GET")

                if (response != null) {
                    val parsedState = parseGameState(response)
                    _gameState.update { currentState ->
                        val narrative = if(isInitialLoad) {
                            // Mantém a narrativa de criação se for o primeiro carregamento
                            if (currentState.narrativeLines.size <= 1) {
                                currentState.narrativeLines
                            } else {
                                listOf("Carregamento concluído. Continue a sua aventura!")
                            }
                        } else {
                            currentState.narrativeLines
                        }
                        parsedState.copy(narrativeLines = narrative)
                    }
                }
            } catch (e: Exception) {
                println("Erro de conexão ao carregar estado: ${e.message}")
            }
        }
    }

    // --- FUNÇÕES AUXILIARES ---

    private fun parseSessionList(jsonString: String): List<SessionInfo> {
        val list = mutableListOf<SessionInfo>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(
                SessionInfo(
                    session_name = jsonObject.getString("session_name"),
                    player_name = jsonObject.getString("player_name"),
                    world_concept = jsonObject.getString("world_concept")
                )
            )
        }
        return list
    }

    private suspend fun makeRequest(url: URL, method: String, body: String? = null): String? {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 15000
                connection.readTimeout = 120000

                if (method == "POST" && body != null) {
                    connection.doOutput = true
                    OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                } else {
                    val errorStream = connection.errorStream?.let {
                        BufferedReader(InputStreamReader(it, "UTF-8")).use { reader -> reader.readText() }
                    }
                    println("Erro na requisição para $url: $responseCode - ${connection.responseMessage} - $errorStream")
                    null
                }
            } catch (e: Exception) {
                println("Exceção na requisição para $url: ${e.message}")
                null
            } finally {
                connection?.disconnect()
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
