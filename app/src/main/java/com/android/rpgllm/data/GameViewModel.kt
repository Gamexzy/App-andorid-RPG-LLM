package com.android.rpgllm.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.rpgllm.data.network.ApiClient
import com.android.rpgllm.data.repository.AuthRepository
import com.android.rpgllm.data.repository.GameRepository
import com.android.rpgllm.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VersionStatus { NONE, CHECKING, UP_TO_DATE, OUTDATED, ERROR }

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // --- Injeção de Dependência (Manual) ---
    private val userPrefsRepository = UserPreferencesRepository(application)
    private val apiClient = ApiClient(userPrefsRepository)
    private val authRepository = AuthRepository(apiClient, userPrefsRepository)
    private val gameRepository = GameRepository(apiClient)

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

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _isEmulatorMode = MutableStateFlow(false)
    val isEmulatorMode: StateFlow<Boolean> = _isEmulatorMode.asStateFlow()

    private val _customIpAddress = MutableStateFlow("")
    val customIpAddress: StateFlow<String> = _customIpAddress.asStateFlow()

    private var currentSessionName: String? = null

    init {
        loadConnectionSettings()
        initializeUserSession() // Função de inicialização principal
    }

    // --- LÓGICA DE AUTENTICAÇÃO E SESSÃO ---
    private fun initializeUserSession() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                // Se já existe um token, apenas atualiza o estado da UI
                _authUiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isAnonymous = userPrefsRepository.isUserAnonymous()
                    )
                }
            } else {
                // Se não há token, tenta logar como anônimo
                val result = authRepository.loginAnonymously()
                if (result is AuthResult.Success) {
                    _authUiState.update { it.copy(isAuthenticated = true, isAnonymous = true) }
                } else {
                    // Falha crítica, não conseguiu nem criar sessão anônima
                    _authUiState.update { it.copy(isAuthenticated = false) }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        _authUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.login(username, password)
            if (result is AuthResult.Success) {
                _authUiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isAnonymous = result.isAnonymous
                    )
                }
            } else if (result is AuthResult.Error) {
                _authUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun register(username: String, password: String) {
        _authUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.register(username, password)
            if (result is AuthResult.Success) {
                // Faz login automaticamente após o registro bem-sucedido
                login(username, password)
            } else if (result is AuthResult.Error) {
                _authUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        // Após o logout, volta para uma nova sessão anônima
        initializeUserSession()
    }

    fun clearAuthError() {
        _authUiState.update { it.copy(errorMessage = null) }
    }

    // ... (O resto do GameViewModel permanece o mesmo)
    fun loadSession(sessionName: String) {
        currentSessionName = sessionName
        val savedHistory = userPrefsRepository.loadChatHistory(sessionName)
        if (savedHistory.isNotEmpty()) {
            _gameState.update { it.copy(narrativeLines = savedHistory, isLoading = false) }
        } else {
            _gameState.update { GameState(narrativeLines = listOf("A carregar saga '$sessionName'...")) }
            fetchGameState(isInitialLoad = true)
        }
        fetchGameState() // Always fetch latest state
    }

    fun createNewSession(characterName: String, worldConcept: String, onSessionCreated: (String) -> Unit) {
        _creationState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            gameRepository.createSession(characterName, worldConcept)
                .onSuccess { (newSessionName, initialNarrative) ->
                    val initialHistory = listOf(initialNarrative)
                    userPrefsRepository.saveChatHistory(newSessionName, initialHistory)
                    _gameState.value = GameState(narrativeLines = initialHistory)
                    onSessionCreated(newSessionName)
                    _creationState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _creationState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

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

    fun onToolSelected(tool: GameTool) {
        _toolMenuState.update { it.copy(isVisible = false) }
        sendPlayerAction(tool.command, displayInLog = false)
    }

    private fun sendPlayerAction(action: String, displayInLog: Boolean) {
        val session = currentSessionName ?: return
        if (action.isBlank()) return

        if (displayInLog) {
            addNarrativeLine("\n\n> $action", session)
        }
        _gameState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            gameRepository.executeTurn(session, action)
                .onSuccess { responseNarrative ->
                    addNarrativeLine("\n\n$responseNarrative", session)
                }
                .onFailure { error ->
                    addNarrativeLine("\n\nErro de Conexão: ${error.message}", session)
                }
            _gameState.update { it.copy(isLoading = false) }
        }
    }

    private fun addNarrativeLine(line: String, session: String) {
        _gameState.update {
            val newHistory = it.narrativeLines + line
            userPrefsRepository.saveChatHistory(session, newHistory)
            it.copy(narrativeLines = newHistory)
        }
    }

    private fun fetchContextualTools() {
        val session = currentSessionName ?: return
        _toolMenuState.update { it.copy(isLoading = true, isVisible = true) }
        viewModelScope.launch {
            gameRepository.getContextualTools(session)
                .onSuccess { tools ->
                    _toolMenuState.update { it.copy(isLoading = false, tools = tools) }
                }
                .onFailure { error ->
                    addNarrativeLine("> Erro ao buscar ações: ${error.message}", session)
                    _toolMenuState.update { it.copy(isLoading = false, isVisible = false) }
                }
        }
    }

    fun fetchGameState(isInitialLoad: Boolean = false) {
        val session = currentSessionName ?: return
        viewModelScope.launch {
            gameRepository.getGameState(session)
                .onSuccess { (base, vitals, possessions) ->
                    _gameState.update { currentState ->
                        val narrative = if (isInitialLoad && currentState.narrativeLines.size <= 1) {
                            currentState.narrativeLines
                        } else {
                            currentState.narrativeLines.ifEmpty { listOf("Carregamento concluído.") }
                        }
                        currentState.copy(base = base, vitals = vitals, possessions = possessions, narrativeLines = narrative)
                    }
                }
                .onFailure { error ->
                    println("Erro de conexão ao carregar estado: ${error.message}")
                }
        }
    }


    // --- FUNÇÕES DE CONFIGURAÇÃO E STATUS ---
    private fun loadConnectionSettings() {
        val (address, isEmulator) = userPrefsRepository.loadConnectionSettings()
        _customIpAddress.value = address
        _isEmulatorMode.value = isEmulator
    }

    fun setCustomIpAddress(address: String) {
        _customIpAddress.value = address
        userPrefsRepository.saveConnectionSettings(address, _isEmulatorMode.value)
    }

    fun toggleEmulatorMode() {
        val newState = !_isEmulatorMode.value
        _isEmulatorMode.value = newState
        userPrefsRepository.saveConnectionSettings(_customIpAddress.value, newState)
    }

    fun fetchSessions() {
        _sessionListState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            gameRepository.getSessions()
                .onSuccess { sessions ->
                    _sessionListState.update { it.copy(sessions = sessions, isLoading = false) }
                }
                .onFailure { error ->
                    _sessionListState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    suspend fun checkAppVersion() {
        _versionStatus.value = VersionStatus.CHECKING
        val (success, result) = gameRepository.getStatus()
        if (success) {
            _versionStatus.value = if (result == "UP_TO_DATE") VersionStatus.UP_TO_DATE else VersionStatus.OUTDATED
        } else {
            _versionStatus.value = VersionStatus.ERROR
        }
    }
}
