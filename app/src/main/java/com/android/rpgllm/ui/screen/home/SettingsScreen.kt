package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            Text("Conexão com o Servidor", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = customIpAddress,
                onValueChange = { gameViewModel.setCustomIpAddress(it) },
                label = { Text("Endereço Remoto (ngrok/VPN)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = settingsOutlinedTextFieldColors()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Modo Emulador (10.0.2.2)", color = Color.Gray, fontSize = 14.sp)
                Switch(
                    checked = isEmulatorMode,
                    onCheckedChange = { gameViewModel.toggleEmulatorMode() },
                    colors = settingsSwitchColors(),
                    enabled = customIpAddress.isBlank()
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { scope.launch { gameViewModel.checkAppVersion() } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Verificar Conexão")
            }
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                when (versionStatus) {
                    VersionStatus.CHECKING -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00C853))
                    VersionStatus.UP_TO_DATE -> Text("Conectado! Versão compatível.", color = Color(0xFF00C853))
                    VersionStatus.OUTDATED -> Text("Atualização Necessária!", color = Color.Yellow)
                    VersionStatus.ERROR -> Text("Erro de Conexão.", color = Color.Red)
                    VersionStatus.NONE -> Text("Verifique a conexão com o servidor.", color = Color.Gray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFF333333))

            Text("Conta", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { gameViewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
            ) {
                Text("Sair (Logout)")
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun settingsOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
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
private fun settingsSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color(0xFF00C853),
    checkedTrackColor = Color(0xFF335C3D),
    uncheckedThumbColor = Color.Gray,
    uncheckedTrackColor = Color(0xFF303030)
)
