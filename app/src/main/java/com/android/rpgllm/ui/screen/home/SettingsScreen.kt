package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.data.VersionStatus
import com.android.rpgllm.navigation.AppRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    navController: NavController
) {
    val authState by gameViewModel.authUiState.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // PARTE MODIFICADA
            .padding(16.dp),
    ) {
        Text("Conta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(16.dp))

        if (authState.isAnonymous) {
            Text(
                "Você está jogando como convidado. Suas sagas são salvas apenas neste dispositivo.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // PARTE MODIFICADA
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
                color = MaterialTheme.colorScheme.primary, // PARTE MODIFICADA
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { gameViewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // PARTE MODIFICADA
            ) {
                Text("Sair (Logout)")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        ConnectionSettings(gameViewModel = gameViewModel)

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ConnectionSettings(gameViewModel: GameViewModel) {
    val isEmulatorMode by gameViewModel.isEmulatorMode.collectAsState()
    val customIpAddress by gameViewModel.customIpAddress.collectAsState()
    val versionStatus by gameViewModel.versionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    Column {
        Text("Conexão com o Servidor", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
            Text("Modo Emulador (10.0.2.2)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // PARTE MODIFICADA
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
                    color = MaterialTheme.colorScheme.primary // MODIFICADO
                )
                VersionStatus.UP_TO_DATE -> Text(
                    "Conectado! Versão compatível.",
                    color = MaterialTheme.colorScheme.primary // MODIFICADO
                )
                VersionStatus.OUTDATED -> Text("Atualização Necessária!", color = Color.Yellow) // Mantido Amarelo para destaque
                VersionStatus.ERROR -> Text("Erro de Conexão.", color = MaterialTheme.colorScheme.error) // MODIFICADO
                VersionStatus.NONE -> Text(
                    "Verifique a conexão com o servidor.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // MODIFICADO
                )
            }
        }
    }
}

@Composable
private fun settingsOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
)

@Composable
private fun settingsSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.primary,
    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
)

@Preview
@Composable
fun SettingsScreenPreview() {
    val gameViewModel: GameViewModel = viewModel()
    val navController = rememberNavController()
    SettingsScreen(gameViewModel = gameViewModel, navController = navController)
}
