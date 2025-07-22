// app/src/main/java/com/android/rpgllm/RpgTextScreen.kt
package com.android.rpgllm

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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    // Função para simular a chamada à API do LLM (substituirá a chamada ao backend Python)
    // No seu ambiente de produção, esta função faria uma requisição HTTP para o seu servidor Python.
    val simulateLLMCall: suspend (String) -> String = { input ->
        val responses = listOf(
            "O vazio se distorce ao seu comando, Gabriel, e antes que perceba, o chão sob seus pés se torna sólido. Você se encontra em uma clareira úmida, onde a luz do sol mal penetra através da densa folhagem. No centro, há uma pedra musgosa e, sobre ela, algo brilha fracamente: um amuleto de quartzo polido, que parece vibrar com uma energia antiga. Ao longe, você ouve o murmúrio de um riacho escondido.",
            "Você pega o amuleto, sentindo um calor suave irradiar dele. A clareira, antes silenciosa, parece sussurrar segredos antigos. O que você faz agora, Gabriel?",
            "Ao se aproximar do riacho, a água cristalina reflete seu rosto, e você percebe que a correnteza é mais forte do que parece. Pequenos peixes prateados nadam contra a corrente. O que você decide fazer com o riacho?",
            "Você bebe da água fresca, sentindo suas energias renovadas. O sabor é puro, como se viesse de uma fonte intocada. Uma sensação de bem-estar o envolve. Para onde você vai agora?",
            "A floresta é densa e escura. Você ouve o farfalhar de folhas e o canto de pássaros desconhecidos. A cada passo, a luz do sol diminui, e a temperatura cai. Você continua a explorar a floresta ou retorna para a clareira?"
        )
        // Simula um atraso de rede
        kotlinx.coroutines.delay(kotlin.random.Random.nextLong(1000, 3000))
        // Retorna uma resposta aleatória para simular a dinâmica
        responses.random()
    }

    // Função para lidar com o envio da ação do jogador
    val sendPlayerAction: () -> Unit = sendPlayerAction@{
        val currentInput = playerInput.trim()
        if (currentInput.isEmpty() || isLoading) {
            return@sendPlayerAction // Não faz nada se o input estiver vazio ou estiver carregando
        }

        playerInput = "" // Limpa o input imediatamente
        isLoading = true // Ativa o estado de carregamento

        // Adiciona a ação do jogador à narrativa
        narrativeLines.add("\n\n\u001b[1;33mSua ação:\u001b[0m $currentInput")
        narrativeLines.add("Mestre de Jogo pensando...") // Indicador de carregamento na narrativa

        coroutineScope.launch(Dispatchers.IO) {
            try {
                // TODO: Substitua esta URL pela URL do seu backend Python
                val url = URL("http://10.0.2.2:5000/execute_turn") // 10.0.2.2 é o IP do host para o emulador Android
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Envia a ação do jogador como JSON
                val jsonInputString = "{\"player_action\": \"$currentInput\"}"
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInputString)
                }

                // Lê a resposta do backend
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        val response = reader.readText()
                        // TODO: Parse a resposta JSON do seu backend para extrair a narrativa
                        // Por enquanto, apenas exibe a resposta bruta
                        withContext(Dispatchers.Main) {
                            // Remove o indicador de "Mestre de Jogo pensando..."
                            if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                                narrativeLines.removeAt(narrativeLines.lastIndex)
                            }
                            narrativeLines.add("\n\n$response")
                        }
                    }
                } else {
                    val errorStream = BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
                    withContext(Dispatchers.Main) {
                        // Remove o indicador de "Mestre de Jogo pensando..."
                        if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                            narrativeLines.removeAt(narrativeLines.lastIndex)
                        }
                        narrativeLines.add("\n\n\u001b[1;31mErro do Servidor ($responseCode):\u001b[0m $errorStream")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Remove o indicador de "Mestre de Jogo pensando..."
                    if (narrativeLines.lastOrNull() == "Mestre de Jogo pensando...") {
                        narrativeLines.removeAt(narrativeLines.lastIndex)
                    }
                    narrativeLines.add("\n\n\u001b[1;31mErro de Conexão:\u001b[0m ${e.message}")
                }
            } finally {
                isLoading = false // Desativa o estado de carregamento
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
                .background(Color(0xFF212121), RoundedCornerShape(12.dp)) // Fundo mais escuro com cantos arredondados
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            color = Color(0xFF212121) // Garante a cor de fundo da Surface
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(narrativeLines) { line ->
                    Text(
                        text = line,
                        color = Color(0xFFE0E0E0), // Texto claro
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Espaço entre a narrativa e o input

        // Indicador de carregamento
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(bottom = 8.dp),
                color = Color(0xFF00C853), // Cor do progresso
                trackColor = Color(0xFF303030) // Cor da trilha
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color(0xFF616161),
                cursorColor = Color(0xFF00C853),
                textColor = Color.White,
                containerColor = Color(0xFF303030), // Cor de fundo do campo
                focusedLabelColor = Color(0xFF00C853),
                unfocusedLabelColor = Color(0xFF9E9E9E)
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { sendPlayerAction() }
            ),
            enabled = !isLoading // Desabilita quando estiver carregando
        )

        // Botão de Envio
        Button(
            onClick = sendPlayerAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading, // Desabilita quando estiver carregando
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), // Cor do botão
            shape = RoundedCornerShape(12.dp), // Cantos arredondados
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
