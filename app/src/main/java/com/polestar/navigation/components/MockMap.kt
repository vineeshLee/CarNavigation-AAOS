package com.polestar.navigation.components

import android.content.pm.PackageManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
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
    }
) {
    val context = LocalContext.current
    val centerLat = 59.33258
    val centerLng = 18.06490

    // Fetch Google Maps API Key securely from the Android Manifest metadata
    val apiKey = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val markersLoaded = remember { mutableStateOf(false) }

    // Map targets for navigation
    val destLatLng = remember(navHUDState.destinationName, fuelStations, restaurants) {
        val destName = navHUDState.destinationName
        val station = fuelStations.find { it.name == destName }
        if (station != null) {
            LatLng(centerLat + station.latOffset * 0.04, centerLng + station.lngOffset * 0.06)
        } else {
            val rest = restaurants.find { it.name == destName }
            if (rest != null) {
                LatLng(centerLat + rest.latOffset * 0.04, centerLng + rest.lngOffset * 0.06)
            } else if (destName == "Home") {
                LatLng(centerLat + 0.02, centerLng + 0.03)
            } else if (destName == "Work") {
                LatLng(centerLat - 0.03, centerLng - 0.025)
            } else {
                LatLng(centerLat + 0.015, centerLng + 0.02)
            }
        }
    }

    // Car coordinates
    val userLatLng = remember(navHUDState.isActive, navHUDState.progress, destLatLng) {
        if (navHUDState.isActive) {
            val p = navHUDState.progress
            LatLng(
                centerLat + p * (destLatLng.latitude - centerLat),
                centerLng + p * (destLatLng.longitude - centerLng)
            )
        } else {
            LatLng(centerLat, centerLng)
        }
    }

    // Observe local camera controllers (zoom +/- / center) and update WebView
    LaunchedEffect(cameraPositionState.position) {
        val webView = webViewRef.value ?: return@LaunchedEffect
        val zoom = cameraPositionState.position.zoom
        val target = cameraPositionState.position.target
        webView.evaluateJavascript(
            "if (map) { map.setZoom($zoom); map.setCenter(new google.maps.LatLng(${target.latitude}, ${target.longitude})); }",
            null
        )
    }

    // Update vehicle position and route line dynamically on the real Google Map
    LaunchedEffect(userLatLng, navHUDState.isActive, destLatLng, markersLoaded.value) {
        val webView = webViewRef.value ?: return@LaunchedEffect
        if (!markersLoaded.value) return@LaunchedEffect

        // Move car marker
        webView.evaluateJavascript(
            "updateCarPosition(${userLatLng.latitude}, ${userLatLng.longitude}, ${if (navHUDState.isActive) 45 else 0})",
            null
        )

        // Draw navigation polyline
        if (navHUDState.isActive) {
            webView.evaluateJavascript(
                "setRoute($centerLat, $centerLng, ${destLatLng.latitude}, ${destLatLng.longitude})",
                null
            )
        } else {
            webView.evaluateJavascript("clearRoute()", null)
        }
    }

    // Load custom markers once the map API is fully loaded
    LaunchedEffect(fuelStations, restaurants, markersLoaded.value) {
        val webView = webViewRef.value ?: return@LaunchedEffect
        if (!markersLoaded.value) return@LaunchedEffect

        fuelStations.forEach { station ->
            val lat = centerLat + station.latOffset * 0.04
            val lng = centerLng + station.lngOffset * 0.06
            val type = if (station.isElectric) "electric" else "gas"
            webView.evaluateJavascript(
                "addMarker('${station.id}', $lat, $lng, '${station.name.replace("'", "\\'")}', '${station.price} | ${station.availability}', '$type')",
                null
            )
        }

        restaurants.forEach { rest ->
            val lat = centerLat + rest.latOffset * 0.04
            val lng = centerLng + rest.lngOffset * 0.06
            webView.evaluateJavascript(
                "addMarker('${rest.id}', $lat, $lng, '${rest.name.replace("'", "\\'")}', '${rest.rating} ★ | ${rest.cuisine}', 'restaurant')",
                null
            )
        }
    }

    val webInterface = remember {
        object {
            @JavascriptInterface
            fun getApiKey(): String = apiKey

            @JavascriptInterface
            fun onMapReady() {
                markersLoaded.value = true
            }

            @JavascriptInterface
            fun onMarkerClick(id: String, type: String) {
                onPinClick(id, type)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    settings.domStorageEnabled = true
                    addJavascriptInterface(webInterface, "AndroidInterface")
                    loadUrl("file:///android_asset/map.html")
                    webViewRef.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
