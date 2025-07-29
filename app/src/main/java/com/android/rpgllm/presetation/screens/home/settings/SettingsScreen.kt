// app/src/main/java/com/android/rpgllm/ui/theme/SettingsScreen.kt
package com.android.rpgllm.presetation.screens.home.settings

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
import com.android.rpgllm.presetation.screens.game.GameViewModel
import com.android.rpgllm.presetation.screens.game.VersionStatus
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
                title = { Text("Configurações de Conexão") },
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
            OutlinedTextField(
                value = customIpAddress,
                onValueChange = { gameViewModel.setCustomIpAddress(it) },
                label = { Text("Endereço Remoto (IP/ngrok)") },
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
                // O campo de IP customizado desabilita o modo emulador
                enabled = !isEmulatorMode
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Usar IP do Emulador (10.0.2.2)", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = isEmulatorMode,
                    onCheckedChange = { gameViewModel.toggleEmulatorMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00C853),
                        checkedTrackColor = Color(0xFF335C3D),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF303030)
                    ),
                    // O switch é desabilitado se um IP customizado estiver preenchido
                    enabled = customIpAddress.isBlank()
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        gameViewModel.checkAppVersion()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Verificar Conexão com o Servidor")
            }
            Spacer(Modifier.height(16.dp))

            when (versionStatus) {
                VersionStatus.CHECKING -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF00C853))
                }
                VersionStatus.UP_TO_DATE -> {
                    Text("Conectado! A versão do servidor é compatível.", color = Color(0xFF00C853), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                VersionStatus.OUTDATED -> {
                    Text("Atualização Necessária! A versão do servidor é mais recente.", color = Color.Yellow, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                VersionStatus.ERROR -> {
                    Text("Erro de Conexão. Verifique o endereço IP e se o servidor está online.", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                VersionStatus.NONE -> {
                    Text("Clique no botão acima para verificar a conexão.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
