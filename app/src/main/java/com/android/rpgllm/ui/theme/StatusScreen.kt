// app/src/main/java/com/android/rpgllm/ui/theme/StatusScreen.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.rpgllm.data.VersionStatus

@Composable
fun StatusScreen(
    status: VersionStatus,
    onRetry: () -> Unit = {}
) {
    val message: String
    val color: Color
    val showErrorContent: Boolean

    when (status) {
        VersionStatus.CHECKING -> {
            message = "Verificando versão..."
            color = Color.White
            showErrorContent = false
        }
        VersionStatus.OUTDATED -> {
            message = "Atualização Necessária!\n\nPor favor, atualize o aplicativo para a versão mais recente para continuar jogando."
            color = Color.Yellow
            showErrorContent = false
        }
        VersionStatus.ERROR -> {
            message = "Erro de conexão.\nVerifique o servidor e a rede."
            color = Color.Red
            showErrorContent = true
        }
        VersionStatus.UP_TO_DATE -> {
            // Este caso é tratado na MainActivity, esta tela não será mostrada.
            // Mas precisamos definir valores padrão.
            message = ""
            color = Color.Transparent
            showErrorContent = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            if (status == VersionStatus.CHECKING) {
                CircularProgressIndicator(color = Color(0xFF00C853))
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = message,
                color = color,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            if (showErrorContent) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Tentar Novamente")
                }
            }
        }
    }
}
