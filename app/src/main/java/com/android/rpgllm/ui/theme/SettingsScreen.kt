package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.VersionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(gameViewModel: GameViewModel) {
    val isEmulatorMode by gameViewModel.isEmulatorMode.collectAsState()
    val customIpAddress by gameViewModel.customIpAddress.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações e Conta") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1E1E1E))
                .padding(16.dp),
        ) {
            // --- Seção de Conexão ---
            Text("Conexão com o Servidor", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = customIpAddress,
                onValueChange = { gameViewModel.setCustomIpAddress(it) },
                label = { Text("Endereço Remoto (ngrok/VPN)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = outlinedTextFieldColors()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Modo Emulador (10.0.2.2)", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = isEmulatorMode,
                    onCheckedChange = { gameViewModel.toggleEmulatorMode() },
                    colors = switchColors(),
                    enabled = customIpAddress.isBlank()
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        gameViewModel.checkAppVersion()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Verificar Conexão")
            }
            Spacer(Modifier.height(16.dp))

            // Indicador de Status da Conexão
            Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                when (versionStatus) {
                    VersionStatus.CHECKING -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00C853))
                    VersionStatus.UP_TO_DATE -> Text("Conectado! Versão compatível.", color = Color(0xFF00C853))
                    VersionStatus.OUTDATED -> Text("Atualização Necessária!", color = Color.Yellow)
                    VersionStatus.ERROR -> Text("Erro de Conexão.", color = Color.Red)
                    VersionStatus.NONE -> Text("Verifique a conexão com o servidor.", color = Color.Gray)
                }
            }

            // --- Divisor e Seção da Conta ---
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFF333333))

            Text("Conta", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // Botão de Logout
            Button(
                onClick = { gameViewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)) // Cor de perigo
            ) {
                Text("Sair (Logout)")
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

// Funções de estilo auxiliares
@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00C853),
    unfocusedBorderColor = Color(0xFF616161),
    cursorColor = Color(0xFF00C853),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0xFF303030),
    unfocusedContainerColor = Color(0xFF303030),
    focusedLabelColor = Color(0xFF00C853),
    unfocusedLabelColor = Color(0xFF9E9E9E)
)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color(0xFF00C853),
    checkedTrackColor = Color(0xFF335C3D),
    uncheckedThumbColor = Color.Gray,
    uncheckedTrackColor = Color(0xFF303030)
)
