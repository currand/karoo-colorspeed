package com.currand60.karoocolorspeed.screens // Or where your ViewModels live


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt annotation for ViewModels
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val karooSystemServiceProvider: provideKarooSystem, // Hilt injects this service
    // Add other dependencies needed by the ViewModel here
) : ViewModel() {

    // Example: Expose the karooDistanceUnit from your service as a StateFlow
    val karooDistanceUnit: StateFlow<KarooSystemServiceProvider.PreferredUnit> =
        karooSystemServiceProvider.streamUserProfile()
            .map { it.preferredUnit.distance } // Map to the specific unit type
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep active while subscribed
                initialValue = KarooSystemServiceProvider.PreferredUnit.IMPERIAL // Initial default value
            )

    // Add other ViewModel logic and state here that your MainScreen needs
}