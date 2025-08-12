package com.android.rpgllm.ui.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
            color = MaterialTheme.colorScheme.onBackground
            showErrorContent = false
        }
        VersionStatus.OUTDATED -> {
            message = "Atualização Necessária!\n\nPor favor, atualize o aplicativo para a versão mais recente para continuar jogando."
            color = Color.Yellow // Mantido para dar destaque
            showErrorContent = false
        }
        VersionStatus.ERROR -> {
            message = "Erro de conexão.\nVerifique o servidor e a rede."
            color = MaterialTheme.colorScheme.error
            showErrorContent = true
        }
        VersionStatus.UP_TO_DATE, VersionStatus.NONE -> {
            message = ""
            color = Color.Transparent
            showErrorContent = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            if (status == VersionStatus.CHECKING) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Tentar Novamente")
                }
            }
        }
    }
}
