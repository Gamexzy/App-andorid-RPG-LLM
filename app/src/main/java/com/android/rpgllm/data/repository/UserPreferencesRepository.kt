package com.android.rpgllm.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class UserPreferencesRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("RPG_LLM_PREFS", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("JWT_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("JWT_TOKEN").apply()
    }

    // Novas funções para gerenciar o estado anônimo
    fun setUserIsAnonymous(isAnonymous: Boolean) {
        sharedPreferences.edit().putBoolean("IS_ANONYMOUS", isAnonymous).apply()
    }

    fun isUserAnonymous(): Boolean {
        // Por padrão, se não houver token, consideramos anônimo.
        return sharedPreferences.getBoolean("IS_ANONYMOUS", getToken() == null)
    }


    fun saveConnectionSettings(address: String, isEmulator: Boolean) {
        sharedPreferences.edit().apply {
            putString("CUSTOM_IP_ADDRESS", address)
            putBoolean("IS_EMULATOR_MODE", isEmulator)
            apply()
        }
    }

    fun loadConnectionSettings(): Pair<String, Boolean> {
        val address = sharedPreferences.getString("CUSTOM_IP_ADDRESS", "") ?: ""
        val isEmulator = sharedPreferences.getBoolean("IS_EMULATOR_MODE", false)
        return Pair(address, isEmulator)
    }

    fun saveChatHistory(sessionName: String, history: List<String>) {
        val jsonArray = JSONArray(history)
        sharedPreferences.edit().putString("CHAT_HISTORY_$sessionName", jsonArray.toString()).apply()
    }

    fun loadChatHistory(sessionName: String): List<String> {
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
}
