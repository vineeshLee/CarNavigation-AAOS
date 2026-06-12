package com.polestar.navigation.data

import androidx.compose.ui.graphics.vector.ImageVector

data class FuelStation(
    val id: String,
    val name: String,
    val distance: String,
    val duration: String,
    val price: String,
    val availability: String,
    val isElectric: Boolean,
    val isBusy: Boolean,
    val latOffset: Float, // for mock map plotting (-1.0 to 1.0)
    val lngOffset: Float
)

data class Restaurant(
    val id: String,
    val name: String,
    val distance: String,
    val rating: Double,
    val priceCategory: String,
    val cuisine: String,
    val status: String,
    val latOffset: Float,
    val lngOffset: Float
)

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FuelFinder : Screen("fuel_finder")
    object RestaurantDiscovery : Screen("restaurant_discovery")
    object NavigationView : Screen("navigation_view")
}

data class NavigationHUDState(
    val isActive: Boolean = false,
    val destinationName: String = "",
    val totalDistance: String = "",
    val totalDuration: String = "",
    val nextTurnDistance: String = "400 m",
    val nextTurnInstruction: String = "Exit right toward E4 South",
    val progress: Float = 0.0f
)
