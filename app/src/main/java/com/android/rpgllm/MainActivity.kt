// app/src/main/java/com/android/rpgllm/MainActivity.kt
package com.android.rpgllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.android.rpgllm.data.GameViewModel // IMPORT ATUALIZADO
import com.android.rpgllm.data.VersionStatus   // IMPORT ATUALIZADO
import com.android.rpgllm.ui.theme.*

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RPGLLMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val versionStatus by gameViewModel.versionStatus.collectAsState()

                    if (versionStatus == VersionStatus.UP_TO_DATE) {
                        MainScreen(gameViewModel = gameViewModel)
                    } else {
                        StatusScreen(
                            status = versionStatus,
                            onRetry = { gameViewModel.checkAppVersion() }
                        )
                    }
                }
            }
        }
    }
}
