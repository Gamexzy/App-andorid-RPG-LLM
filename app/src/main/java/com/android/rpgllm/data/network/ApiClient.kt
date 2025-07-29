package com.android.rpgllm.data.network

import com.android.rpgllm.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiClient(private val userPreferencesRepository: UserPreferencesRepository) {

    // Retorna a URL base atual para as chamadas de API.
    private fun getCurrentBaseUrl(): String {
        val (customAddress, isEmulator) = userPreferencesRepository.loadConnectionSettings()
        if (customAddress.startsWith("http://") || customAddress.startsWith("https://")) {
            return customAddress.removeSuffix("/")
        }
        val finalIp = when {
            customAddress.isNotEmpty() -> customAddress
            isEmulator -> "10.0.2.2"
            else -> "192.168.0.104" // IP Padrão para dispositivo físico
        }
        return "http://$finalIp:5000"
    }

    /**
     * Realiza uma requisição HTTP genérica para a API.
     * @param endpoint O caminho do endpoint (ex: "/login").
     * @param method O método HTTP (ex: "GET", "POST").
     * @param body O corpo da requisição em formato JSON (para POST/PUT).
     * @param requiresAuth Se a requisição exige um token de autenticação.
     * @return A resposta do servidor como String ou um JSON de erro.
     */
    suspend fun makeRequest(
        endpoint: String,
        method: String,
        body: String? = null,
        requiresAuth: Boolean = true
    ): String {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("${getCurrentBaseUrl()}$endpoint")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 15000
                connection.readTimeout = 120000 // 2 minutos

                if (requiresAuth) {
                    val token = userPreferencesRepository.getToken()
                        ?: throw Exception("Token de autenticação ausente.")
                    connection.setRequestProperty("Authorization", "Bearer $token")
                }

                if ((method == "POST" || method == "PUT") && body != null) {
                    connection.doOutput = true
                    OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(body) }
                }

                if (connection.responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                } else {
                    val errorStream = connection.errorStream?.let {
                        BufferedReader(InputStreamReader(it, "UTF-8")).use { br -> br.readText() }
                    }
                    println("HTTP Error ${connection.responseCode}: $errorStream")
                    errorStream ?: "{\"error\": \"Erro HTTP ${connection.responseCode}\"}"
                }
            } catch (e: Exception) {
                println("Request Exception: ${e.message}")
                "{\"error\": \"Erro de conexão: ${e.message}\"}"
            } finally {
                connection?.disconnect()
            }
        }
    }
}
