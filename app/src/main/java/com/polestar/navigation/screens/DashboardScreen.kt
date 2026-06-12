package com.polestar.navigation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polestar.navigation.components.MockMap
import com.polestar.navigation.data.FuelStation
import com.polestar.navigation.data.NavigationHUDState
import com.polestar.navigation.data.Restaurant
import com.polestar.navigation.data.Screen
import com.polestar.navigation.theme.GraphiteCard
import com.polestar.navigation.theme.KineticGold
import com.polestar.navigation.theme.OutlineBorder
import com.polestar.navigation.theme.TextPrimary
import com.polestar.navigation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    fuelStations: List<FuelStation>,
    restaurants: List<Restaurant>,
    navHUDState: NavigationHUDState,
    vehicleSpeed: Int,
    onNavigateToScreen: (Screen) -> Unit,
    onStartNavigation: (String, String, String) -> Unit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isMediaPlaying by remember { mutableStateOf(false) }
    var showSafetyAlert by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column - Media & Quick Actions (1/3 width)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media Player Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GraphiteCard),
                border = BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album Art Placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = KineticGold,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        IconButton(onClick = { /* Media menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary)
                        }
                    }

                    Column {
                        Text(
                            text = "Midnight City",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "M83 — Hurry Up, We're Dreaming",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = 0.35f,
                            color = KineticGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                        )

                        // Media Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { /* Skip previous */ },
                                modifier = Modifier.size(64.dp) // Large touch target
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = TextPrimary, modifier = Modifier.size(36.dp))
                            }

                            // Play Button
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(KineticGold)
                                    .clickable { isMediaPlaying = !isMediaPlaying },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMediaPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(
                                onClick = { /* Skip next */ },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextPrimary, modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            // Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = {
                        onStartNavigation("Home", "12.4 km", "18 min")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Work,
                    label = "Work",
                    onClick = {
                        onStartNavigation("Work", "18.2 km", "25 min")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.LocalGasStation,
                    label = "Fuel",
                    onClick = {
                        onNavigateToScreen(Screen.FuelFinder)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Right Column - MockMap & HUD Overlays (2/3 width)
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, OutlineBorder, RoundedCornerShape(12.dp))
        ) {
            MockMap(
                fuelStations = fuelStations,
                restaurants = restaurants,
                navHUDState = navHUDState,
                onPinClick = { _, _ -> }
            )

            // Search Bar Overlay (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .width(360.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        if (vehicleSpeed == 0) {
                            searchQuery = it
                        } else {
                            showSafetyAlert = true
                        }
                    },
                    placeholder = { Text("Search destination...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    trailingIcon = { Icon(Icons.Default.Mic, contentDescription = "Voice", tint = TextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
                        unfocusedBorderColor = OutlineBorder,
                        focusedBorderColor = KineticGold,
                        textColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable {
                            if (vehicleSpeed > 0) {
                                showSafetyAlert = true
                            }
                        },
                    enabled = vehicleSpeed == 0 // Disable typing when driving
                )
            }

            // Navigation HUD Overlay (Top Left)
            AnimatedVisibility(
                visible = navHUDState.isActive,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.width(280.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(KineticGold, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TurnRight,
                                contentDescription = "Turn Right",
                                tint = OnKineticGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column {
                            Text(
                                text = navHUDState.nextTurnDistance,
                                color = TextPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = navHUDState.nextTurnInstruction,
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Navigation Route Info (Bottom Left)
            AnimatedVisibility(
                visible = navHUDState.isActive,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Arrival", color = TextSecondary, fontSize = 12.sp)
                                Text("14:55", color = KineticGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Distance", color = TextSecondary, fontSize = 12.sp)
                                Text(navHUDState.totalDistance, color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = onStopNavigation,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Exit Route", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }

            // Map Zoom Controls (Bottom Right)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapIconButton(icon = Icons.Default.Add, onClick = {})
                MapIconButton(icon = Icons.Default.Remove, onClick = {})
                MapIconButton(icon = Icons.Default.MyLocation, onClick = {}, isFeatured = true)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = GraphiteCard),
        border = BorderStroke(1.dp, OutlineBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = KineticGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun MapIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isFeatured: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isFeatured) KineticGold else Color.Black.copy(alpha = 0.8f))
            .border(1.dp, if (isFeatured) Color.Transparent else Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFeatured) OnKineticGold else TextPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}
