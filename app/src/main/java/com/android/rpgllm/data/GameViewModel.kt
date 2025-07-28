// app/src/main/java/com/android/rpgllm/data/GameViewModel.kt
package com.android.rpgllm.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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

// As classes de dados foram movidas para Models.kt

enum class VersionStatus { NONE, CHECKING, UP_TO_DATE, OUTDATED, ERROR }

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("RPG_LLM_PREFS", Context.MODE_PRIVATE)

    // --- Estados da UI ---
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _sessionListState = MutableStateFlow(SessionListUiState())
    val sessionListState: StateFlow<SessionListUiState> = _sessionListState.asStateFlow()

    private val _creationState = MutableStateFlow(CreationUiState())
    val creationState: StateFlow<CreationUiState> = _creationState.asStateFlow()

    private val _versionStatus = MutableStateFlow(VersionStatus.NONE)
    val versionStatus: StateFlow<VersionStatus> = _versionStatus.asStateFlow()

    private val _toolMenuState = MutableStateFlow(ToolMenuUiState())
    val toolMenuState: StateFlow<ToolMenuUiState> = _toolMenuState.asStateFlow()

    // --- Configurações de Conexão ---
    private val _isEmulatorMode = MutableStateFlow(false)
    val isEmulatorMode: StateFlow<Boolean> = _isEmulatorMode.asStateFlow()

    private val _customIpAddress = MutableStateFlow("")
    val customIpAddress: StateFlow<String> = _customIpAddress.asStateFlow()

    private var currentSessionName: String? = null
    private val emulatorIp = "10.0.2.2"
    private val physicalDeviceIp = "192.168.0.104" // Lembre-se de ajustar se necessário

    init {
        loadIpAddress()
    }

    // --- LÓGICA DE NAVEGAÇÃO E SESSÃO ---

    fun loadSession(sessionName: String) {
        currentSessionName = sessionName
        val savedHistory = loadChatHistory(sessionName)
        if (savedHistory.isNotEmpty()) {
            _gameState.update { it.copy(narrativeLines = savedHistory, isLoading = false) }
        } else {
            _gameState.update { GameState(narrativeLines = listOf("A carregar saga '$sessionName'...")) }
            fetchGameState(isInitialLoad = true)
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

                    val initialHistory = listOf(initialNarrative)
                    saveChatHistory(newSessionName, initialHistory)
                    _gameState.value = GameState(narrativeLines = initialHistory)
                    onSessionCreated(newSessionName)
                } else {
                    _creationState.update { it.copy(isLoading = false, errorMessage = "Falha ao criar sessão.") }
                }
            } catch (e: Exception) {
                _creationState.update { it.copy(isLoading = false, errorMessage = "Erro de conexão: ${e.message}") }
            } finally {
                _creationState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- LÓGICA DE REDE E JOGO ---

    fun processPlayerInput(input: String) {
        val trimmedInput = input.trim()
        if (trimmedInput.equals("/tools", ignoreCase = true)) {
            if (_toolMenuState.value.isVisible) {
                _toolMenuState.update { it.copy(isVisible = false) }
            } else {
                fetchContextualTools()
            }
        } else {
            if (_toolMenuState.value.isVisible) {
                _toolMenuState.update { it.copy(isVisible = false) }
            }
            sendPlayerAction(trimmedInput, displayInLog = true)
        }
    }

    private fun fetchContextualTools() {
        val session = currentSessionName ?: return
        _toolMenuState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/tools")
                val response = makeRequest(url, "GET")
                if (response != null) {
                    val tools = parseTools(response)
                    _toolMenuState.update { it.copy(isLoading = false, tools = tools, isVisible = true) }
                } else {
                    addNarrativeLine("> Não foi possível obter as ações contextuais.", session)
                    _toolMenuState.update { it.copy(isLoading = false, isVisible = false) }
                }
            } catch (e: Exception) {
                addNarrativeLine("> Erro de conexão ao buscar ações: ${e.message}", session)
                _toolMenuState.update { it.copy(isLoading = false, isVisible = false) }
            }
        }
    }

    fun onToolSelected(tool: GameTool) {
        _toolMenuState.update { it.copy(isVisible = false) }
        sendPlayerAction(tool.command, displayInLog = false)
    }

    private fun sendPlayerAction(action: String, displayInLog: Boolean) {
        val session = currentSessionName ?: return
        if (action.isBlank()) return

        _gameState.update { currentState ->
            val updatedLines = if (displayInLog) {
                currentState.narrativeLines + "\n\n> $action"
            } else {
                currentState.narrativeLines
            }
            currentState.copy(isLoading = true, narrativeLines = updatedLines)
        }

        viewModelScope.launch {
            val responseNarrative: String = try {
                val url = URL("${getCurrentBaseUrl()}/sessions/$session/execute_turn")
                val payload = JSONObject().apply { put("player_action", action) }
                val response = makeRequest(url, "POST", payload.toString())
                JSONObject(response ?: "{}").optString("narrative", "Erro: 'narrative' não encontrada.")
            } catch (e: Exception) {
                "Erro de Conexão: ${e.message}"
            }
            addNarrativeLine("\n\n$responseNarrative", session)
            _gameState.update { it.copy(isLoading = false) }
        }
    }

    private fun addNarrativeLine(line: String, session: String?) {
        session ?: return
        _gameState.update {
            val newHistory = it.narrativeLines + line
            saveChatHistory(session, newHistory)
            it.copy(narrativeLines = newHistory)
        }
    }

    // --- FUNÇÕES DE PERSISTÊNCIA ---

    private fun saveIpAddress(address: String) {
        with(sharedPreferences.edit()) {
            putString("CUSTOM_IP_ADDRESS", address)
            apply()
        }
    }

    private fun loadIpAddress() {
        val savedIp = sharedPreferences.getString("CUSTOM_IP_ADDRESS", "") ?: ""
        _customIpAddress.value = savedIp
    }

    private fun saveChatHistory(sessionName: String, history: List<String>) {
        val jsonArray = JSONArray(history)
        with(sharedPreferences.edit()) {
            putString("CHAT_HISTORY_$sessionName", jsonArray.toString())
            apply()
        }
    }

    private fun loadChatHistory(sessionName: String): List<String> {
        val jsonString = sharedPreferences.getString("CHAT_HISTORY_$sessionName", null)
        return if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun setCustomIpAddress(address: String) {
        _customIpAddress.value = address
        saveIpAddress(address)
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
                        _sessionListState.update { it.copy(isLoading = false, errorMessage = "Nenhuma resposta do servidor.") }
                    }
                } catch (e: Exception) {
                    _sessionListState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            } else {
                _sessionListState.update { it.copy(isLoading = false) }
            }
        }
    }

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

    fun toggleEmulatorMode() { _isEmulatorMode.value = !_isEmulatorMode.value }

    private fun getCurrentBaseUrl(): String {
        val customAddress = _customIpAddress.value.trim()
        if (customAddress.startsWith("http://") || customAddress.startsWith("https://")) {
            return customAddress.removeSuffix("/")
        }
        val finalIp = when {
            customAddress.isNotEmpty() -> customAddress
            _isEmulatorMode.value -> emulatorIp
            else -> physicalDeviceIp
        }
        return "http://$finalIp:5000"
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
                        val narrative = if(isInitialLoad && currentState.narrativeLines.size <= 1) {
                            currentState.narrativeLines
                        } else {
                            currentState.narrativeLines.ifEmpty { listOf("Carregamento concluído.") }
                        }
                        parsedState.copy(narrativeLines = narrative)
                    }
                }
            } catch (e: Exception) {
                println("Erro de conexão ao carregar estado: ${e.message}")
            }
        }
    }

    private fun parseTools(jsonString: String): List<GameTool> {
        val tools = mutableListOf<GameTool>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                tools.add(
                    GameTool(
                        displayName = jsonObject.getString("displayName"),
                        command = jsonObject.getString("command")
                    )
                )
            }
        } catch (e: Exception) {
            println("Erro ao parsear as ferramentas: $e")
        }
        return tools
    }

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
                    OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(body) }
                }
                if (connection.responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
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
