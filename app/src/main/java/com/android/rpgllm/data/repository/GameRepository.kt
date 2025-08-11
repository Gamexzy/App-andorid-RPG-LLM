package com.android.rpgllm.data.repository

import com.android.rpgllm.BuildConfig
import com.android.rpgllm.data.GameTool
import com.android.rpgllm.data.PlayerBase
import com.android.rpgllm.data.PlayerPossession
import com.android.rpgllm.data.PlayerVitals
import com.android.rpgllm.data.AdventureInfo
import com.android.rpgllm.data.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

class GameRepository(private val apiClient: ApiClient) {

    suspend fun getStatus(): Pair<Boolean, String> {
        return try {
            val response = apiClient.makeRequest("/status", "GET", requiresAuth = false)
            val serverInfo = JSONObject(response)
            if (serverInfo.has("error")) {
                return Pair(false, serverInfo.getString("error"))
            }
            val minVersion = serverInfo.getString("minimum_client_version")
            if (BuildConfig.VERSION_NAME.toDouble() >= minVersion.toDouble()) {
                Pair(true, "UP_TO_DATE")
            } else {
                Pair(true, "OUTDATED")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Erro desconhecido")
        }
    }

    suspend fun getAdventures(): Result<List<AdventureInfo>> {
        return try {
            val response = apiClient.makeRequest("/adventures", "GET")
            val jsonArray = JSONArray(response)
            val list = mutableListOf<AdventureInfo>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                list.add(
                    AdventureInfo(
                        adventureName = jsonObject.getString("adventure_name"),
                        playerName = jsonObject.getString("player_name"),
                        worldConcept = jsonObject.getString("world_concept")
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NOVA FUNÇÃO ---
    // Envia uma requisição para apagar uma saga no servidor.
    suspend fun deleteAdventure(adventureName: String): Result<Unit> {
        return try {
            val response = apiClient.makeRequest("/Adventures/$adventureName", "DELETE")
            val jsonResponse = JSONObject(response)
            if (jsonResponse.has("error")) {
                throw Exception(jsonResponse.getString("error"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdventure(characterName: String, worldConcept: String): Result<Pair<String, String>> {
        return try {
            val payload = JSONObject().apply {
                put("character_name", characterName)
                put("world_concept", worldConcept)
            }
            val response = apiClient.makeRequest("/adventures/create", "POST", payload.toString())
            val jsonResponse = JSONObject(response)
            if (jsonResponse.has("error")) {
                throw Exception(jsonResponse.getString("error"))
            }
            val newAdventureName = jsonResponse.getString("adventure_name")
            val initialNarrative = jsonResponse.getString("initial_narrative")
            Result.success(Pair(newAdventureName, initialNarrative))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeTurn(adventureName: String, action: String): Result<String> {
        return try {
            val payload = JSONObject().apply { put("player_action", action) }
            val response = apiClient.makeRequest("/adventures/$adventureName/execute_turn", "POST", payload.toString())
            val narrative = JSONObject(response).optString("narrative", "Erro: 'narrative' não encontrada.")
            Result.success(narrative)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContextualTools(adventureName: String): Result<List<GameTool>> {
        return try {
            val response = apiClient.makeRequest("/adventures/$adventureName/tools", "GET")
            val jsonArray = JSONArray(response)
            val tools = mutableListOf<GameTool>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                tools.add(GameTool(displayName = jsonObject.getString("displayName"), command = jsonObject.getString("command")))
            }
            Result.success(tools)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameState(adventureName: String): Result<Triple<PlayerBase, PlayerVitals, List<PlayerPossession>>> {
        return try {
            val response = apiClient.makeRequest("/adventures/$adventureName/state", "GET")
            val json = JSONObject(response)

            val baseJson = json.optJSONObject("base") ?: JSONObject()
            val playerBase = PlayerBase(
                nome = baseJson.optString("nome", "N/A"),
                localNome = baseJson.optString("local_nome", "N/A")
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
            Result.success(Triple(playerBase, playerVitals, possessionsList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
