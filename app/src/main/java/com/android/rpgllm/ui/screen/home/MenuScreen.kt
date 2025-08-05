package com.android.rpgllm.ui.screen.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.android.rpgllm.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onCloseMenu: () -> Unit
) {
    Scaffold(
        modifier = modifier, // Aplica o modificador da animação
        topBar = {
            TopAppBar(
                title = { Text("Gerenciamento") },
                navigationIcon = {
                    IconButton(onClick = onCloseMenu) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fechar Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
        ) {
            NavigationDrawerItem(
                label = { Text("Aventuras") },
                selected = false,
                onClick = onCloseMenu,
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aventuras") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Personagens") },
                selected = false,
                onClick = { navController.navigate(AppRoutes.CHARACTERS_SCREEN) },
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Personagens") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Universo") },
                selected = false,
                onClick = { navController.navigate(AppRoutes.UNIVERSES_SCREEN) },
                icon = { Icon(Icons.Default.Public, contentDescription = "Universo") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
