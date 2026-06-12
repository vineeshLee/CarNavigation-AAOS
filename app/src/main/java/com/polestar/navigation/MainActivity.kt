package com.polestar.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.polestar.navigation.components.SafetyBanner
import com.polestar.navigation.components.Sidebar
import com.polestar.navigation.components.TopBar
import com.polestar.navigation.data.NavigationViewModel
import com.polestar.navigation.data.Screen
import com.polestar.navigation.screens.DashboardScreen
import com.polestar.navigation.screens.FuelFinderScreen
import com.polestar.navigation.screens.NavigationViewScreen
import com.polestar.navigation.screens.RestaurantDiscoveryScreen
import com.polestar.navigation.theme.CarNavigationTheme
import com.polestar.navigation.theme.GraphiteCard
import com.polestar.navigation.theme.KineticGold
import com.polestar.navigation.theme.TextPrimary
import com.polestar.navigation.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private val viewModel: NavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force the Maps SDK to use the legacy renderer to run on emulators without play-services updates
        com.google.android.gms.maps.MapsInitializer.initialize(
            applicationContext, 
            com.google.android.gms.maps.MapsInitializer.Renderer.LEGACY
        ) { renderer ->
            android.util.Log.d("MapsInit", "Renderer version: $renderer")
        }

        setContent {
            CarNavigationTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                
                val currentScreen = when (currentBackStackEntry?.destination?.route) {
                    Screen.Dashboard.route -> Screen.Dashboard
                    Screen.FuelFinder.route -> Screen.FuelFinder
                    Screen.RestaurantDiscovery.route -> Screen.RestaurantDiscovery
                    Screen.NavigationView.route -> Screen.NavigationView
                    else -> Screen.Dashboard
                }

                val fuelStations by viewModel.fuelStations.collectAsState()
                val restaurants by viewModel.restaurants.collectAsState()
                val navHUDState by viewModel.navHUDState.collectAsState()
                val vehicleSpeed by viewModel.vehicleSpeed.collectAsState()

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Left Sidebar
                    Sidebar(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Right Workspace Panel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val screenTitle = when (currentScreen) {
                            Screen.Dashboard -> "Polestar OS Dashboard"
                            Screen.FuelFinder -> "Fuel & Charging Stations"
                            Screen.RestaurantDiscovery -> "Nearby Discovery"
                            Screen.NavigationView -> "Navigation View"
                        }
                        TopBar(title = screenTitle)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(Screen.Dashboard.route) {
                                    DashboardScreen(
                                        fuelStations = fuelStations,
                                        restaurants = restaurants,
                                        navHUDState = navHUDState,
                                        vehicleSpeed = vehicleSpeed,
                                        onNavigateToScreen = { screen ->
                                            navController.navigate(screen.route)
                                        },
                                        onStartNavigation = { name, dist, dur ->
                                            viewModel.startNavigation(name, dist, dur)
                                            navController.navigate(Screen.NavigationView.route)
                                        },
                                        onStopNavigation = {
                                            viewModel.stopNavigation()
                                        }
                                    )
                                }

                                composable(Screen.FuelFinder.route) {
                                    FuelFinderScreen(
                                        fuelStations = fuelStations,
                                        restaurants = restaurants,
                                        navHUDState = navHUDState,
                                        onStartNavigation = { name, dist, dur ->
                                            viewModel.startNavigation(name, dist, dur)
                                            navController.navigate(Screen.NavigationView.route)
                                        }
                                    )
                                }

                                composable(Screen.RestaurantDiscovery.route) {
                                    RestaurantDiscoveryScreen(
                                        restaurants = restaurants,
                                        onStartNavigation = { name, dist, dur ->
                                            viewModel.startNavigation(name, dist, dur)
                                            navController.navigate(Screen.NavigationView.route)
                                        }
                                    )
                                }

                                composable(Screen.NavigationView.route) {
                                    NavigationViewScreen(
                                        fuelStations = fuelStations,
                                        restaurants = restaurants,
                                        navHUDState = navHUDState,
                                        vehicleSpeed = vehicleSpeed,
                                        onNavigateToScreen = { screen ->
                                            navController.navigate(screen.route)
                                        },
                                        onStartNavigation = { name, dist, dur ->
                                            viewModel.startNavigation(name, dist, dur)
                                        },
                                        onStopNavigation = {
                                            viewModel.stopNavigation()
                                        }
                                    )
                                }
                            }

                            // Driver Safety Warning Banner (Overlay on top if driving)
                            SafetyBanner(
                                speed = vehicleSpeed,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                            )

                            // Speed Safety lock simulation widget
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GraphiteCard.copy(alpha = 0.9f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(horizontal = 32.dp, vertical = 96.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = if (vehicleSpeed > 0) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (vehicleSpeed > 0) MaterialTheme.colorScheme.error else KineticGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (vehicleSpeed > 0) "Status: DRIVING" else "Status: PARKED",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (vehicleSpeed > 0) "Speed Lock Active" else "Inputs Unlocked",
                                            color = if (vehicleSpeed > 0) MaterialTheme.colorScheme.error else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = {
                                            if (vehicleSpeed > 0) {
                                                viewModel.setSpeed(0)
                                            } else {
                                                viewModel.setSpeed(65)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (vehicleSpeed > 0) KineticGold else MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Text(
                                            text = if (vehicleSpeed > 0) "Park Car" else "Simulate Drive",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (vehicleSpeed > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
