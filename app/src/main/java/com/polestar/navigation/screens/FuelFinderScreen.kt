package com.polestar.navigation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polestar.navigation.components.MockMap
import com.polestar.navigation.data.FuelStation
import com.polestar.navigation.data.NavigationHUDState
import com.polestar.navigation.data.Restaurant
import com.polestar.navigation.theme.GraphiteCard
import com.polestar.navigation.theme.KineticGold
import com.polestar.navigation.theme.OutlineBorder
import com.polestar.navigation.theme.TextPrimary
import com.polestar.navigation.theme.TextSecondary

@Composable
fun FuelFinderScreen(
    fuelStations: List<FuelStation>,
    restaurants: List<Restaurant>,
    navHUDState: NavigationHUDState,
    onStartNavigation: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left - Station list (width 450dp)
        Column(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Stations",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GraphiteCard)
                        .clickable { /* filter */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextPrimary)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(fuelStations) { station ->
                    val isBusy = station.isBusy
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isBusy) 0.65f else 1.0f),
                        colors = CardDefaults.cardColors(containerColor = GraphiteCard),
                        border = BorderStroke(1.dp, OutlineBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = station.name,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "${station.duration} • ${station.distance}",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                // Status Badge
                                val badgeColor = if (isBusy) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                val badgeText = if (isBusy) "Busy" else station.availability
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badgeColor)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = if (isBusy) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (station.isElectric) "Price per kWh" else "Regular Gas",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = station.price,
                                        color = if (station.isElectric) KineticGold else TextPrimary,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        onStartNavigation(station.name, station.distance, station.duration)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isBusy) Color(0xFF4D4D4D) else KineticGold),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.height(48.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "GO",
                                            color = if (isBusy) TextPrimary else MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = null,
                                            tint = if (isBusy) TextPrimary else MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = OutlineBorder
        )

        // Right - Map View & Battery HUD
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            MockMap(
                fuelStations = fuelStations,
                restaurants = restaurants,
                navHUDState = navHUDState,
                onPinClick = { _, _ -> }
            )

            // Battery warning card (bottom right)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
                    .width(360.dp),
                colors = CardDefaults.cardColors(containerColor = GraphiteCard),
                border = BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF2D3400), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricCar,
                                contentDescription = null,
                                tint = KineticGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Battery Status",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "34% (124 mi range)",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = 0.34f,
                        color = KineticGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = "Charging is recommended within 20 miles.",
                        color = KineticGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Map Controls (Floating, Right Center)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapIconButton(icon = Icons.Default.MyLocation, onClick = {})
                MapIconButton(icon = Icons.Default.Add, onClick = {})
                MapIconButton(icon = Icons.Default.Remove, onClick = {})
            }
        }
    }
}
