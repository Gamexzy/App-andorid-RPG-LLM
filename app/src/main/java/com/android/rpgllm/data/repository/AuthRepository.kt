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
        return performAuthRequest("/login", username, password)
    }

    suspend fun register(username: String, password: String): AuthResult {
        return performAuthRequest("/register", username, password)
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
                AuthResult.Success
            } else {
                AuthResult.Error(jsonResponse.optString("error", "Erro desconhecido."))
            }
        } catch (e: Exception) {
            AuthResult.Error("Erro de conexão: ${e.message}")
        }
    }
}
