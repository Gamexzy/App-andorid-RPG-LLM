// app/src/main/java/com/android/rpgllm/ui/theme/DrawerContent.kt
package com.android.rpgllm.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(
    gameState: GameState,
    isEmulatorMode: Boolean,
    onToggleEmulatorMode: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.background(Color(0xFF1E1E1E))
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Ícone do Personagem",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF00C853)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = gameState.base.nome,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Seção: Ficha do Personagem
            item {
                SectionTitle("Ficha do Personagem")
                InfoRow(label = "Localização:", value = gameState.base.local_nome)
                InfoRow(label = "Fome:", value = gameState.vitals.fome)
                InfoRow(label = "Sede:", value = gameState.vitals.sede)
                InfoRow(label = "Cansaço:", value = gameState.vitals.cansaco)
                InfoRow(label = "Humor:", value = gameState.vitals.humor)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Seção: Inventário
            item { SectionTitle("Inventário") }
            if (gameState.possessions.isEmpty()) {
                item { Text("Vazio", color = Color.Gray, modifier = Modifier.padding(8.dp)) }
            } else {
                items(gameState.possessions) { possession -> InventoryItemRow(item = possession) }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- NOVA SEÇÃO: CONFIGURAÇÕES ---
            item {
                SectionTitle("Configurações")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Modo Emulador", color = Color.Gray, fontSize = 14.sp)
                    Switch(
                        checked = isEmulatorMode,
                        onCheckedChange = { onToggleEmulatorMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00C853),
                            checkedTrackColor = Color(0xFF335C3D),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF303030)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF00C853),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    HorizontalDivider(color = Color(0xFF333333))
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InventoryItemRow(item: PlayerPossession) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Ícone do item",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = item.itemName, color = Color.White, fontSize = 14.sp)
    }
}
