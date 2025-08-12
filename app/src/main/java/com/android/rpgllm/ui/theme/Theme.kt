package com.android.rpgllm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de cores escuras personalizada para o app
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C853), // Verde principal
    secondary = Color(0xFF64ffda), // Verde secundário
    tertiary = Color(0xFFb9f6ca), // Verde mais claro
    background = Color(0xFF121212), // Fundo principal escuro
    surface = Color(0xFF1E1E1E), // Cor para cards e superfícies elevadas
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFB71C1C)
)

// A paleta de cores claras pode ser mantida para consistência, mas o app foca no tema escuro.
private val LightColorScheme = lightColorScheme(
    primary = DarkBackground,
    secondary = LightGreen,
    tertiary = LighterGreen,
)

@Composable
fun RPGLLMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desativado para forçar nosso tema customizado
    content: @Composable () -> Unit
) {
    // Forçando o tema escuro, já que é o design principal do app
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Define a cor da barra de status e de navegação
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            // Define os ícones da barra de status e navegação para a cor clara (para contrastar com o fundo escuro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
