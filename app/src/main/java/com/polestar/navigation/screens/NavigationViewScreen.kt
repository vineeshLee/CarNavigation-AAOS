package com.polestar.navigation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun NavigationViewScreen(
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
    var showSafetyAlert by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Background Map
        MockMap(
            fuelStations = fuelStations,
            restaurants = restaurants,
            navHUDState = navHUDState,
            onPinClick = { _, _ -> }
        )

        // 1. Turn-by-Turn Guidance Panel (Top Left)
        AnimatedVisibility(
            visible = navHUDState.isActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .width(420.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Primary turn block
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(KineticGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TurnSlightRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Column {
                            Text(
                                text = navHUDState.nextTurnDistance,
                                color = TextPrimary,
                                style = MaterialTheme.typography.displayLarge,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 40.sp
                            )
                            Text(
                                text = navHUDState.nextTurnInstruction,
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Secondary guidance snippet
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(start = 16.dp).width(360.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Straight, contentDescription = null, tint = KineticGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Then continue for 12.4 km",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // 2. Destination Search Input (Top Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .width(480.dp)
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
                placeholder = { Text("Where to?", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    unfocusedBorderColor = OutlineBorder,
                    focusedBorderColor = KineticGold,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable {
                        if (vehicleSpeed > 0) {
                            showSafetyAlert = true
                        }
                    },
                enabled = vehicleSpeed == 0
            )
        }

        // 3. Route Progress HUD (Bottom Left)
        AnimatedVisibility(
            visible = navHUDState.isActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .width(320.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("14:55", color = KineticGold, style = MaterialTheme.typography.displayLarge, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            Text("Arrival Time", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("33 min", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("18.4 km", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = navHUDState.progress,
                        color = KineticGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )

                    Button(
                        onClick = onStopNavigation,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Exit Route", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Quick Category Filters (Bottom Center)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MapFilterButton(
                icon = Icons.Default.LocalGasStation,
                label = "Gas",
                onClick = { onNavigateToScreen(Screen.FuelFinder) }
            )
            MapFilterButton(
                icon = Icons.Default.EvStation,
                label = "Charging",
                onClick = { onNavigateToScreen(Screen.FuelFinder) }
            )
            MapFilterButton(
                icon = Icons.Default.LocalParking,
                label = "Parking",
                onClick = {}
            )
            MapFilterButton(
                icon = Icons.Default.Restaurant,
                label = "Food",
                onClick = { onNavigateToScreen(Screen.RestaurantDiscovery) }
            )
        }

        // 5. Map Action Controls (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MapIconButton(icon = Icons.Default.Navigation, onClick = {})
            
            // Zoom panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextPrimary)
                    }
                    Divider(color = OutlineBorder, modifier = Modifier.width(56.dp).height(1.dp))
                    IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextPrimary)
                    }
                }
            }

            MapIconButton(icon = Icons.Default.Layers, onClick = {})
        }
    }
}

@Composable
fun MapFilterButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = KineticGold, modifier = Modifier.size(20.dp))
            Text(text = label, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}
