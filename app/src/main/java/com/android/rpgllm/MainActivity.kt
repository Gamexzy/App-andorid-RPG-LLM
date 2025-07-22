// app/src/main/java/com/android/rpgllm/MainActivity.kt
package com.android.rpgllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.rpgllm.ui.theme.RPGLLMTheme // Importa o seu tema
import com.android.rpgllm.ui.theme.RpgTextScreen // Importa RpgTextScreen do pacote ui.theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica o tema do seu aplicativo (definido em ui.theme/Theme.kt)
            RPGLLMTheme {
                // Uma superfície de contêiner usando a cor 'background' do tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Chama a função Composable que constrói a tela principal do RPG
                    RpgTextScreen()
                }
            }
        }
    }
}
