package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.VersionStatus
import com.android.rpgllm.navigation.AppRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gameViewModel: GameViewModel,
    navController: NavController // Precisa do NavController para ir para a tela de Auth
) {
    val authState by gameViewModel.authUiState.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conta e Conexão") },
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
            // --- Seção da Conta ---
            Text("Conta", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            if (authState.isAnonymous) {
                Text(
                    "Você está jogando como convidado. Suas sagas são salvas apenas neste dispositivo.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(AppRoutes.AUTH) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar ou Criar Conta")
                }
            } else {
                Text(
                    "Você está logado. Suas sagas estão sincronizadas na nuvem.",
                    color = Color(0xFF00C853),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { gameViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Sair (Logout)")
                }
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFF333333))

            // --- Seção de Conexão ---
            ConnectionSettings(gameViewModel = gameViewModel)

            Spacer(Modifier.weight(1f))
        }
    }
}

// Componente extraído para manter o código limpo
@Composable
private fun ConnectionSettings(gameViewModel: GameViewModel) {
    val isEmulatorMode by gameViewModel.isEmulatorMode.collectAsState()
    val customIpAddress by gameViewModel.customIpAddress.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Column {
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
            colors = settingsOutlinedTextFieldColors()
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp), contentAlignment = Alignment.Center
        ) {
            when (versionStatus) {
                VersionStatus.CHECKING -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF00C853)
                )
                VersionStatus.UP_TO_DATE -> Text(
                    "Conectado! Versão compatível.",
                    color = Color(0xFF00C853)
                )
                VersionStatus.OUTDATED -> Text("Atualização Necessária!", color = Color.Yellow)
                VersionStatus.ERROR -> Text("Erro de Conexão.", color = Color.Red)
                VersionStatus.NONE -> Text(
                    "Verifique a conexão com o servidor.",
                    color = Color.Gray
                )
            }
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
