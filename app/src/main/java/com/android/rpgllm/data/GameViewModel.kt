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

    private val _adventureListState = MutableStateFlow(AdventureListUiState())
    val adventureListState: StateFlow<AdventureListUiState> = _adventureListState.asStateFlow()

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

    private var currentAdventureName: String? = null

    init {
        loadConnectionSettings()
        initializeUserAdventure()
    }

    // --- LÓGICA DE AUTENTICAÇÃO E SESSÃO ---
    private fun initializeUserAdventure() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                _authUiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isAnonymous = userPrefsRepository.isUserAnonymous()
                    )
                }
            } else {
                val result = authRepository.loginAnonymously()
                if (result is AuthResult.Success) {
                    _authUiState.update { it.copy(isAuthenticated = true, isAnonymous = true) }
                } else {
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
                login(username, password)
            } else if (result is AuthResult.Error) {
                _authUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        initializeUserAdventure()
    }

    fun clearAuthError() {
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun loadAdventure(adventureName: String) {
        currentAdventureName = adventureName
        val savedHistory = userPrefsRepository.loadChatHistory(adventureName)
        if (savedHistory.isNotEmpty()) {
            _gameState.update { it.copy(narrativeLines = savedHistory, isLoading = false) }
        } else {
            _gameState.update { GameState(narrativeLines = listOf("A carregar saga '$adventureName'...")) }
            fetchGameState(isInitialLoad = true)
        }
        fetchGameState()
    }

    fun deleteAdventure(adventureName: String) {
        viewModelScope.launch {
            gameRepository.deleteAdventure(adventureName)
                .onSuccess {
                    userPrefsRepository.deleteChatHistory(adventureName)
                    fetchAdventures()
                }
                .onFailure { error ->
                    _adventureListState.update { it.copy(errorMessage = "Falha ao apagar: ${error.message}") }
                }
        }
    }

    // --- FUNÇÃO ATUALIZADA ---
    fun createNewAdventure(
        characterName: String,
        characterClass: String,
        characterBackstory: String,
        worldConcept: String,
        onAdventureCreated: (String) -> Unit
    ) {
        _creationState.update { it.copy(isLoading = true, errorMessage = null) }

        // Combina as informações para enviar ao backend
        val fullCharacterInfo = "Nome: $characterName\nClasse: $characterClass\nHistória: $characterBackstory"
        val fullWorldInfo = "Conceito do Mundo: $worldConcept"

        viewModelScope.launch {
            // A API do repositório ainda espera 2 argumentos, então concatenamos a informação.
            // O ideal no futuro seria atualizar a API para aceitar os campos separadamente.
            gameRepository.createAdventure(fullCharacterInfo, fullWorldInfo)
                .onSuccess { (newAdventureName, initialNarrative) ->
                    val initialHistory = listOf(initialNarrative)
                    userPrefsRepository.saveChatHistory(newAdventureName, initialHistory)
                    _gameState.value = GameState(narrativeLines = initialHistory)
                    onAdventureCreated(newAdventureName)
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
        val adventure = currentAdventureName ?: return
        if (action.isBlank()) return

        if (displayInLog) {
            addNarrativeLine("\n\n> $action", adventure)
        }
        _gameState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            gameRepository.executeTurn(adventure, action)
                .onSuccess { responseNarrative ->
                    addNarrativeLine("\n\n$responseNarrative", adventure)
                }
                .onFailure { error ->
                    addNarrativeLine("\n\nErro de Conexão: ${error.message}", adventure)
                }
            _gameState.update { it.copy(isLoading = false) }
        }
    }

    private fun addNarrativeLine(line: String, adventure: String) {
        _gameState.update {
            val newHistory = it.narrativeLines + line
            userPrefsRepository.saveChatHistory(adventure, newHistory)
            it.copy(narrativeLines = newHistory)
        }
    }

    private fun fetchContextualTools() {
        val adventure = currentAdventureName ?: return
        _toolMenuState.update { it.copy(isLoading = true, isVisible = true) }
        viewModelScope.launch {
            gameRepository.getContextualTools(adventure)
                .onSuccess { tools ->
                    _toolMenuState.update { it.copy(isLoading = false, tools = tools) }
                }
                .onFailure { error ->
                    addNarrativeLine("> Erro ao buscar ações: ${error.message}", adventure)
                    _toolMenuState.update { it.copy(isLoading = false, isVisible = false) }
                }
        }
    }

    fun fetchGameState(isInitialLoad: Boolean = false) {
        val adventure = currentAdventureName ?: return
        viewModelScope.launch {
            gameRepository.getGameState(adventure)
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

    fun fetchAdventures() {
        _adventureListState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            gameRepository.getAdventures()
                .onSuccess { adventures ->
                    _adventureListState.update { it.copy(adventures = adventures, isLoading = false) }
                }
                .onFailure { error ->
                    _adventureListState.update { it.copy(isLoading = false, errorMessage = error.message) }
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
