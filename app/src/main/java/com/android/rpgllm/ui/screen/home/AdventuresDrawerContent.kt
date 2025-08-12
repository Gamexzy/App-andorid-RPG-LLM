package com.android.rpgllm.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.rpgllm.navigation.AppRoutes

@Composable
fun AdventuresDrawerContent(
    navController: NavController,
    onCloseDrawer: () -> Unit
) {
    // PARTE MODIFICADA: Adicionado cor de fundo e conteúdo
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.7f),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Gerenciamento",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        // PARTE MODIFICADA
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        // Item para Aventuras - Apenas fecha a gaveta
        NavigationDrawerItem(
            label = { Text("Aventuras") },
            selected = false, // A seleção é controlada pela BottomBar
            onClick = {
                onCloseDrawer()
            },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aventuras") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // Item para Personagens - Usa a rota correta de AppRoutes
        NavigationDrawerItem(
            label = { Text("Personagens") },
            selected = navController.currentDestination?.route == AppRoutes.CHARACTERS_SCREEN,
            onClick = {
                navController.navigate(AppRoutes.CHARACTERS_SCREEN)
                onCloseDrawer()
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Personagens") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // Item para Universo - Usa a rota correta de AppRoutes
        NavigationDrawerItem(
            label = { Text("Universo") },
            selected = navController.currentDestination?.route == AppRoutes.UNIVERSES_SCREEN,
            onClick = {
                navController.navigate(AppRoutes.UNIVERSES_SCREEN)
                onCloseDrawer()
            },
            icon = { Icon(Icons.Default.Public, contentDescription = "Universo") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
