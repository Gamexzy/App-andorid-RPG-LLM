// app/src/main/java/com/android/rpgllm/MainActivity.kt
package com.android.rpgllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.rpgllm.ui.theme.RPGLLMTheme
import com.android.rpgllm.ui.theme.MainScreen // Importa a nova tela principal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RPGLLMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Chama a nova tela que contém a aba lateral e a tela do RPG
                    MainScreen()
                }
            }
        }
    }
}
