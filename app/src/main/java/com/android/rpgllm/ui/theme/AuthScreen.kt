// app/src/main/java/com/android/rpgllm/ui/theme/AuthScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    gameViewModel: GameViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val authState by gameViewModel.authUiState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRegisterMode) "Criar Conta" else "Login") },
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Universo Emergente",
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de Usuário") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00C853))
            } else {
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            gameViewModel.register(username, password)
                        } else {
                            gameViewModel.login(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text(if (isRegisterMode) "Registrar" else "Entrar")
                }
            }

            authState.errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    gameViewModel.clearAuthError()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "Já tem uma conta? Faça login" else "Não tem uma conta? Registre-se",
                    color = Color.Gray
                )
            }
        }
    }
}

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
