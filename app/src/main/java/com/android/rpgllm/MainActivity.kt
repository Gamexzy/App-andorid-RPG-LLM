// app/src/main/java/com/android/rpgllm/MainActivity.kt
package com.android.rpgllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.rpgllm.data.GameViewModel
import com.android.rpgllm.navigation.AppNavigation
import com.android.rpgllm.ui.theme.RPGLLMTheme

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
                    // A navegação agora gerencia o fluxo de autenticação
                    AppNavigation(gameViewModel = gameViewModel)
                }
            }
        }
    }
}
