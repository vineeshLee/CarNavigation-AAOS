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

class NavigationViewModel : ViewModel() {

    // Speed simulation states (Showcases Driver safety)
    private val _vehicleSpeed = MutableStateFlow(0) // in km/h
    val vehicleSpeed: StateFlow<Int> = _vehicleSpeed.asStateFlow()

    // Screen states
    private val _fuelStations = MutableStateFlow<List<FuelStation>>(emptyList())
    val fuelStations: StateFlow<List<FuelStation>> = _fuelStations.asStateFlow()

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

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
                _navHUDState.update { state ->
                    state.copy(
                        progress = currentProgress.coerceAtMost(1.0f),
                        nextTurnDistance = if (currentProgress < 0.3f) "400 m" else if (currentProgress < 0.7f) "150 m" else "50 m",
                        nextTurnInstruction = if (currentProgress < 0.3f) "Exit right toward E4 South" else if (currentProgress < 0.7f) "Prepare to merge onto Route 222" else "Turn right onto Stockholm Blvd"
                    )
                }
            }
            // Arrived! Reset navigation
            stopNavigation()
        }
    }

    fun stopNavigation() {
        navigationJob?.cancel()
        _navHUDState.value = NavigationHUDState(isActive = false)
        setSpeed(0)
    }

    fun setSpeed(speed: Int) {
        _vehicleSpeed.value = speed
    }
}
