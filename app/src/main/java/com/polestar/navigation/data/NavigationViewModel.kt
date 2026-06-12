package com.polestar.navigation.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class NavigationViewModel : ViewModel() {

    // Speed simulation states (Showcases Driver safety)
    private val _vehicleSpeed = MutableStateFlow(0) // in km/h
    val vehicleSpeed: StateFlow<Int> = _vehicleSpeed.asStateFlow()

    // Screen states
    private val _fuelStations = MutableStateFlow<List<FuelStation>>(emptyList())
    val fuelStations: StateFlow<List<FuelStation>> = _fuelStations.asStateFlow()

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // Current coordinates (Stockholm Central default)
    private val _currentLocation = MutableStateFlow(LatLngState(59.33258, 18.06490))
    val currentLocation: StateFlow<LatLngState> = _currentLocation.asStateFlow()

    // Active destination coordinates
    private val _destinationLocation = MutableStateFlow(LatLngState(59.33258, 18.06490))
    val destinationLocation: StateFlow<LatLngState> = _destinationLocation.asStateFlow()

    // Navigation states
    private val _navHUDState = MutableStateFlow(NavigationHUDState())
    val navHUDState: StateFlow<NavigationHUDState> = _navHUDState.asStateFlow()

    private var navigationJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        _fuelStations.value = listOf(
            FuelStation("1", "Ionity Ultra Fast", "8 min", "1.2 mi", "$0.42", "12/16 Free", isElectric = true, isBusy = false, latOffset = -0.2f, lngOffset = 0.3f),
            FuelStation("2", "Circle K Metro", "14 min", "3.5 mi", "$4.89", "Available", isElectric = false, isBusy = false, latOffset = 0.4f, lngOffset = 0.6f),
            FuelStation("3", "Tesla Supercharger", "18 min", "5.1 mi", "$0.58", "Busy", isElectric = true, isBusy = true, latOffset = -0.6f, lngOffset = -0.4f)
        )

        _restaurants.value = listOf(
            Restaurant("1", "L'Artiste Nordic", "2.4 mi", 4.8, "$$$", "Italian", "Open until 10 PM", latOffset = -0.5f, lngOffset = 0.5f),
            Restaurant("2", "Umami Collective", "0.8 mi", 4.9, "$$$$", "Japanese Fusion", "15 min wait", latOffset = 0.2f, lngOffset = -0.2f),
            Restaurant("3", "Chrome Roasters", "1.2 mi", 4.5, "$$", "Cafe", "Quick Stop", latOffset = 0.5f, lngOffset = 0.1f),
            Restaurant("4", "The Obsidian Room", "3.1 mi", 4.7, "$$$", "Modern Bistro", "Table Ready", latOffset = -0.3f, lngOffset = -0.7f),
            Restaurant("5", "Helix Gastronomy", "5.2 mi", 5.0, "$$$$$", "Fine Dining", "Booking Required", latOffset = 0.7f, lngOffset = -0.5f),
            Restaurant("6", "The Green Anchor", "1.5 mi", 4.6, "$$", "Vegan", "Open Now", latOffset = -0.1f, lngOffset = 0.8f)
        )
    }

    fun startNavigation(destinationName: String, distance: String, duration: String) {
        val station = _fuelStations.value.find { it.name == destinationName }
        if (station != null) {
            _destinationLocation.value = LatLngState(59.33258 + station.latOffset * 0.04, 18.06490 + station.lngOffset * 0.06)
        } else {
            val rest = _restaurants.value.find { it.name == destinationName }
            if (rest != null) {
                _destinationLocation.value = LatLngState(59.33258 + rest.latOffset * 0.04, 18.06490 + rest.lngOffset * 0.06)
            } else if (destinationName == "Home") {
                _destinationLocation.value = LatLngState(59.33258 + 0.02, 18.06490 + 0.03)
            } else if (destinationName == "Work") {
                _destinationLocation.value = LatLngState(59.33258 - 0.03, 18.06490 - 0.025)
            }
        }

        val startLat = 59.33258
        val startLng = 18.06490
        val dest = _destinationLocation.value

        navigationJob?.cancel()
        _navHUDState.value = NavigationHUDState(
            isActive = true,
            destinationName = destinationName,
            totalDistance = distance,
            totalDuration = duration,
            progress = 0.0f
        )
        
        // Auto-simulate driving speed when navigation begins
        setSpeed(65)

        navigationJob = viewModelScope.launch {
            var currentProgress = 0.0f
            while (currentProgress < 1.0f) {
                delay(3000)
                currentProgress += 0.05f
                val p = currentProgress.coerceAtMost(1.0f)
                _currentLocation.value = LatLngState(
                    startLat + p * (dest.latitude - startLat),
                    startLng + p * (dest.longitude - startLng)
                )
                _navHUDState.update { state ->
                    state.copy(
                        progress = p,
                        nextTurnDistance = if (p < 0.3f) "400 m" else if (p < 0.7f) "150 m" else "50 m",
                        nextTurnInstruction = if (p < 0.3f) "Exit right toward E4 South" else if (p < 0.7f) "Prepare to merge onto Route 222" else "Turn right onto Stockholm Blvd"
                    )
                }
            }
            // Arrived! Reset navigation
            stopNavigation()
        }
    }

    fun searchAndNavigate(query: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = java.net.URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "CarNavigationApp/1.0")
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val latIndex = response.indexOf("\"lat\":\"")
                val lonIndex = response.indexOf("\"lon\":\"")
                if (latIndex != -1 && lonIndex != -1) {
                    val latStart = latIndex + 7
                    val latEnd = response.indexOf("\"", latStart)
                    val latStr = response.substring(latStart, latEnd)
                    
                    val lonStart = lonIndex + 7
                    val lonEnd = response.indexOf("\"", lonStart)
                    val lonStr = response.substring(lonStart, lonEnd)
                    
                    val lat = latStr.toDouble()
                    val lon = lonStr.toDouble()
                    
                    withContext(Dispatchers.Main) {
                        _destinationLocation.value = LatLngState(lat, lon)
                        startNavigation(query, "15.0 km", "20 min")
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopNavigation() {
        navigationJob?.cancel()
        _navHUDState.value = NavigationHUDState(isActive = false)
        _currentLocation.value = LatLngState(59.33258, 18.06490) // reset to Stockholm default
        setSpeed(0)
    }

    fun setSpeed(speed: Int) {
        _vehicleSpeed.value = speed
    }
}
