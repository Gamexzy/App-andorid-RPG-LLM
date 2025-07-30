package com.android.rpgllm.data.repository

import com.android.rpgllm.data.AuthResult
import com.android.rpgllm.data.network.ApiClient
import org.json.JSONObject

class AuthRepository(
    private val apiClient: ApiClient,
    private val prefsRepository: UserPreferencesRepository
) {

    fun isLoggedIn(): Boolean {
        return prefsRepository.getToken() != null
    }

    fun logout() {
        prefsRepository.clearToken()
    }

    suspend fun login(username: String, password: String): AuthResult {
        val result = performAuthRequest("/login", username, password)
        // Após o login bem-sucedido, marcamos que o usuário não é mais anônimo
        if (result is AuthResult.Success) {
            prefsRepository.setUserIsAnonymous(false)
            return AuthResult.Success(isAnonymous = false)
        }
        return result
    }

    suspend fun register(username: String, password: String): AuthResult {
        return performAuthRequest("/register", username, password)
    }

    // Nova função para login anônimo
    suspend fun loginAnonymously(): AuthResult {
        return try {
            // Esta chamada assume que o seu backend tem um endpoint para registrar/logar usuários anônimos
            val response = apiClient.makeRequest("/login/anonymous", "POST", requiresAuth = false)
            val jsonResponse = JSONObject(response)

            if (!jsonResponse.has("error")) {
                val token = jsonResponse.optString("token", null)
                if (token != null) {
                    prefsRepository.saveToken(token)
                    prefsRepository.setUserIsAnonymous(true) // Marca como anônimo
                }
                AuthResult.Success(isAnonymous = true)
            } else {
                AuthResult.Error(jsonResponse.optString("error", "Erro ao criar sessão de convidado."))
            }
        } catch (e: Exception) {
            AuthResult.Error("Erro de conexão: ${e.message}")
        }
    }

    private suspend fun performAuthRequest(endpoint: String, username: String, password: String): AuthResult {
        return try {
            val payload = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            val response = apiClient.makeRequest(endpoint, "POST", payload.toString(), requiresAuth = false)
            val jsonResponse = JSONObject(response)

            if (!jsonResponse.has("error")) {
                val token = jsonResponse.optString("token", null)
                if (token != null) {
                    prefsRepository.saveToken(token)
                }
                AuthResult.Success(isAnonymous = false)
            } else {
                AuthResult.Error(jsonResponse.optString("error", "Erro desconhecido."))
            }
        } catch (e: Exception) {
            AuthResult.Error("Erro de conexão: ${e.message}")
        }
    }
}
