package com.polestar.navigation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.polestar.navigation.R
import com.polestar.navigation.data.FuelStation
import com.polestar.navigation.data.NavigationHUDState
import com.polestar.navigation.data.Restaurant

@Composable
fun MockMap(
    fuelStations: List<FuelStation>,
    restaurants: List<Restaurant>,
    navHUDState: NavigationHUDState,
    onPinClick: (String, String) -> Unit, // id, type ("fuel" or "restaurant")
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(59.33258, 18.06490), 14f)
    },
    currentLocation: LatLng = LatLng(59.33258, 18.06490),
    destinationLocation: LatLng = LatLng(59.33258, 18.06490)
) {
    val context = LocalContext.current

    // Properties with our custom Obsidian style
    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style),
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        )
    }

    val userLatLng = currentLocation
    val destLatLng = destinationLocation

    // Follow the vehicle coordinate dynamically
    LaunchedEffect(userLatLng) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLng(userLatLng)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // 1. Draw Route line if active
            if (navHUDState.isActive) {
                Polyline(
                    points = listOf(
                        LatLng(59.33258, 18.06490),
                        destLatLng
                    ),
                    color = Color(0xFF00E5FF),
                    width = 8f
                )

                // Active route glow effect
                Polyline(
                    points = listOf(
                        LatLng(59.33258, 18.06490),
                        destLatLng
                    ),
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    width = 16f
                )

                // Destination marker
                val destMarkerState = rememberMarkerState(key = "dest", position = destLatLng)
                destMarkerState.position = destLatLng
                Marker(
                    state = destMarkerState,
                    title = navHUDState.destinationName,
                    snippet = "Destination",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            // 2. Add Fuel Station Markers
            fuelStations.forEach { station ->
                val pos = LatLng(59.33258 + station.latOffset * 0.04, 18.06490 + station.lngOffset * 0.06)
                val markerState = rememberMarkerState(key = "fuel_${station.id}", position = pos)
                markerState.position = pos
                val markerColor = if (station.isElectric) {
                    BitmapDescriptorFactory.HUE_YELLOW // Electric Charging Gold/Yellow
                } else {
                    BitmapDescriptorFactory.HUE_AZURE // Gas Station Blue
                }

                Marker(
                    state = markerState,
                    title = station.name,
                    snippet = "${station.price} | ${station.availability}",
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                    onClick = {
                        onPinClick(station.id, "fuel")
                        false
                    }
                )
            }

            // 3. Add Restaurant Markers
            restaurants.forEach { rest ->
                val pos = LatLng(59.33258 + rest.latOffset * 0.04, 18.06490 + rest.lngOffset * 0.06)
                val markerState = rememberMarkerState(key = "rest_${rest.id}", position = pos)
                markerState.position = pos

                Marker(
                    state = markerState,
                    title = rest.name,
                    snippet = "${rest.rating} ★ | ${rest.cuisine}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE), // Pink/Peach
                    onClick = {
                        onPinClick(rest.id, "restaurant")
                        false
                    }
                )
            }

            // 4. Vehicle location marker
            val carMarkerState = rememberMarkerState(key = "car", position = userLatLng)
            carMarkerState.position = userLatLng
            Marker(
                state = carMarkerState,
                title = "Your Polestar",
                snippet = if (navHUDState.isActive) "Driving" else "Parked",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }
    }
}
