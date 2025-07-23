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
import com.android.rpgllm.data.GameState
import com.android.rpgllm.data.PlayerPossession

@Composable
fun DrawerContent(
    gameState: GameState,
    isEmulatorMode: Boolean,
    onToggleEmulatorMode: () -> Unit,
    customIpAddress: String, // NOVO
    onCustomIpChange: (String) -> Unit // NOVO
) {
    ModalDrawerSheet(
        modifier = Modifier.background(Color(0xFF1E1E1E))
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            // ... (Cabeçalho, Ficha e Inventário permanecem iguais) ...
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(Icons.Default.Face, "Ícone", Modifier.size(48.dp), tint = Color(0xFF00C853))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(gameState.base.nome, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                SectionTitle("Ficha do Personagem")
                InfoRow(label = "Localização:", value = gameState.base.local_nome)
                InfoRow(label = "Fome:", value = gameState.vitals.fome)
                InfoRow(label = "Sede:", value = gameState.vitals.sede)
                InfoRow(label = "Cansaço:", value = gameState.vitals.cansaco)
                InfoRow(label = "Humor:", value = gameState.vitals.humor)
                Spacer(modifier = Modifier.height(24.dp))
            }
            item { SectionTitle("Inventário") }
            if (gameState.possessions.isEmpty()) {
                item { Text("Vazio", color = Color.Gray, modifier = Modifier.padding(8.dp)) }
            } else {
                items(gameState.possessions) { p -> InventoryItemRow(item = p) }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }


            // --- SEÇÃO DE CONFIGURAÇÕES ATUALIZADA ---
            item {
                SectionTitle("Configurações de Conexão")

                // Campo para IP customizado (ngrok)
                OutlinedTextField(
                    value = customIpAddress,
                    onValueChange = onCustomIpChange,
                    label = { Text("Endereço Remoto (ngrok/VPN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00C853),
                        unfocusedBorderColor = Color(0xFF616161),
                        cursorColor = Color(0xFF00C853),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF303030),
                        unfocusedContainerColor = Color(0xFF303030),
                        focusedLabelColor = Color(0xFF00C853),
                        unfocusedLabelColor = Color(0xFF9E9E9E)
                    )
                )

                // Switch para modo Emulador
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Modo Emulador (10.0.2.2)", color = Color.Gray, fontSize = 14.sp)
                    Switch(
                        checked = isEmulatorMode,
                        onCheckedChange = { onToggleEmulatorMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00C853),
                            checkedTrackColor = Color(0xFF335C3D),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF303030)
                        ),
                        // Desabilita o switch se um IP customizado estiver em uso
                        enabled = customIpAddress.isBlank()
                    )
                }
            }
        }
    }
}

// ... (O resto do arquivo permanece igual) ...
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
