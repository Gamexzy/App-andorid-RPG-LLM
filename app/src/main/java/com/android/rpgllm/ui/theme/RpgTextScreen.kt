// app/src/main/java/com/android/rpgllm/ui/theme/RpgTextScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpgTextScreen() {
    // Estado para armazenar as linhas da narrativa
    val narrativeLines = remember { mutableStateListOf<String>() }
    // Estado para o input do jogador
    var playerInput by remember { mutableStateOf("") }
    // Estado para controlar o carregamento (enquanto espera a resposta do LLM)
    var isLoading by remember { mutableStateOf(false) }
    // Estado para controlar o scroll da narrativa
    val listState = rememberLazyListState()
    // Escopo da coroutine para chamadas de rede
    val coroutineScope = rememberCoroutineScope()

    // Efeito para rolar para o final da narrativa sempre que ela é atualizada
    LaunchedEffect(narrativeLines.size) {
        if (narrativeLines.isNotEmpty()) {
            listState.animateScrollToItem(narrativeLines.lastIndex)
        }
    }

    // Adiciona a mensagem inicial ao carregar a tela
    LaunchedEffect(Unit) {
        narrativeLines.add("Bem-vindo ao universo! Para começar, diga-me seu nome ou descreva o que você gostaria de ser.")
    }

    // Função para lidar com o envio da ação do jogador
    val sendPlayerAction: () -> Unit = sendPlayerAction@{
        val currentInput = playerInput.trim()
        if (currentInput.isEmpty() || isLoading) {
            return@sendPlayerAction // Não faz nada se o input estiver vazio ou estiver carregando
        }

        playerInput = "" // Limpa o input imediatamente
        isLoading = true // Ativa o estado de carregamento

        // Adiciona a ação do jogador à narrativa (sem códigos de cor)
        narrativeLines.add("\n\nSua ação: $currentInput")
        narrativeLines.add("Mestre de Jogo pensando...") // Indicador de carregamento na narrativa

        coroutineScope.launch(Dispatchers.IO) {
            try {
                // --- IMPORTANTE: CONFIGURAÇÃO DE REDE ---
                // 1. Se estiver usando o Emulador Android, o IP 10.0.2.2 é o correto.
                // 2. Se estiver usando um CELULAR FÍSICO, substitua "10.0.2.2" pelo
                //    endereço de IP do seu COMPUTADOR na sua rede Wi-Fi (ex: "192.168.1.5").
                // 3. Certifique-se que o celular e o computador estão na MESMA rede Wi-Fi.
                val url = URL("http://192.168.0.104:5000/execute_turn")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000 // 15 segundos
                connection.readTimeout = 60000 // 60 segundos

                // Envia a ação do jogador como JSON
                val jsonInputString = JSONObject().apply {
                    put("player_action", currentInput)
                }.toString()

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                        val responseBody = reader.readText()
                        val responseJson = JSONObject(responseBody)
                        val narrative = responseJson.optString("narrative", "Erro: 'narrative' não encontrada na resposta.")

                        withContext(Dispatchers.Main) {
                            if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                                narrativeLines.removeAt(narrativeLines.lastIndex)
                            }
                            narrativeLines.add("\n\n$narrative")
                        }
                    }
                } else {
                    val errorStream = connection.errorStream?.let {
                        BufferedReader(InputStreamReader(it, "UTF-8")).use { reader -> reader.readText() }
                    } ?: "Nenhuma informação de erro adicional."
                    withContext(Dispatchers.Main) {
                        if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                            narrativeLines.removeAt(narrativeLines.lastIndex)
                        }
                        // Adiciona mensagem de erro (sem códigos de cor)
                        narrativeLines.add("\n\nErro do Servidor ($responseCode): $errorStream")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                        narrativeLines.removeAt(narrativeLines.lastIndex)
                    }
                    // Adiciona mensagem de erro (sem códigos de cor)
                    narrativeLines.add("\n\nErro de Conexão: Verifique se o servidor Python está rodando e se o dispositivo tem acesso à rede. (${e.message})")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Cor de fundo escura
            .padding(16.dp)
    ) {
        // Título do Aplicativo
        Text(
            text = "RPG de Texto: O Universo Emergente",
            color = Color(0xFF00C853), // Cor verde esmeralda
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Área da Narrativa
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF212121), RoundedCornerShape(12.dp))
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            color = Color(0xFF212121)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(narrativeLines) { line ->
                    Text(
                        text = line,
                        color = Color(0xFFE0E0E0),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(bottom = 8.dp),
                color = Color(0xFF00C853),
                trackColor = Color(0xFF303030)
            )
        }

        // Campo de Entrada do Jogador
        OutlinedTextField(
            value = playerInput,
            onValueChange = { playerInput = it },
            label = { Text("Digite sua ação aqui...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color(0xFF616161),
                cursorColor = Color(0xFF00C853),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF303030),
                unfocusedContainerColor = Color(0xFF303030),
                focusedLabelColor = Color(0xFF00C853),
                unfocusedLabelColor = Color(0xFF9E9E9E)
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { sendPlayerAction() }
            ),
            enabled = !isLoading
        )

        // Botão de Envio
        Button(
            onClick = sendPlayerAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = if (isLoading) "Enviando..." else "Enviar Ação",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
