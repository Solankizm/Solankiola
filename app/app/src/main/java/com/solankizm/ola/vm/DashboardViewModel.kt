package com.solankizm.ola.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solankizm.ola.data.DemoVehicleSource
import com.solankizm.ola.data.VehicleData
import com.solankizm.ola.data.VehicleSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { DASHBOARD, TRIP, CHARGING, RANGE, SETTINGS }

class DashboardViewModel @JvmOverloads constructor(
    private val source: VehicleSource = DemoVehicleSource(),
) : ViewModel() {

    val vehicle: StateFlow<VehicleData> =
        source.stream.stateIn(viewModelScope, SharingStarted.Eagerly, VehicleData())

    private val _screen = MutableStateFlow(Screen.DASHBOARD)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _dark = MutableStateFlow(true)
    val dark: StateFlow<Boolean> = _dark.asStateFlow()

    val sourceName: String get() = source.name

    fun go(screen: Screen) {
        _screen.value = screen
    }

    fun toggleTheme() = viewModelScope.launch {
        _dark.value = !_dark.value
    }
}
