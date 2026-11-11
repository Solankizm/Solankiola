package com.solankizm.ola.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solankizm.ola.data.DemoVehicleSource
import com.solankizm.ola.data.VehicleData
import com.solankizm.ola.data.VehicleSource
import com.solankizm.ola.data.net.ConnectState
import com.solankizm.ola.data.net.S1Client
import com.solankizm.ola.data.net.readDeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { DASHBOARD, TRIP, CHARGING, RANGE, CONNECT, SETTINGS }

class DashboardViewModel @JvmOverloads constructor(
    private val source: VehicleSource = DemoVehicleSource(),
) : ViewModel() {

    val vehicle: StateFlow<VehicleData> =
        source.stream.stateIn(viewModelScope, SharingStarted.Eagerly, VehicleData())

    private val _screen = MutableStateFlow(Screen.DASHBOARD)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _dark = MutableStateFlow(true)
    val dark: StateFlow<Boolean> = _dark.asStateFlow()

    // ---- Phone -> scooter live link -----------------------------------------
    private val _conn = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val conn: StateFlow<ConnectState> = _conn.asStateFlow()
    private var client: S1Client? = null

    private val _discovering = MutableStateFlow(false)
    val discovering: StateFlow<Boolean> = _discovering.asStateFlow()
    private val _discovered = MutableStateFlow<String?>(null)
    val discovered: StateFlow<String?> = _discovered.asStateFlow()

    val sourceName: String get() = source.name

    fun go(screen: Screen) { _screen.value = screen }

    fun toggleTheme() = viewModelScope.launch { _dark.value = !_dark.value }

    /** Connect to the scooter's diagnostics daemon and read real device info. */
    fun connect(ip: String, scope: Int?) = viewModelScope.launch(Dispatchers.IO) {
        if (ip.isBlank()) {
            _conn.value = ConnectState.Error("Enter the scooter's IP address first.")
            return@launch
        }
        _conn.value = ConnectState.Connecting
        try {
            client?.close()
            val c = S1Client(host = ip.trim(), port = 50005, scope = scope)
            c.connect(timeoutSeconds = 8L)
            client = c
            _conn.value = ConnectState.Connected(readDeviceInfo(c))
        } catch (e: Exception) {
            client?.close(); client = null
            _conn.value = ConnectState.Error(e.message ?: "Could not reach the scooter.")
        }
    }

    fun refreshInfo() = viewModelScope.launch(Dispatchers.IO) {
        val c = client ?: return@launch
        runCatching { _conn.value = ConnectState.Connected(readDeviceInfo(c)) }
    }

    /** Scan the hotspot/Wi-Fi for a scooter answering on port 50005. */
    fun autoDiscover() = viewModelScope.launch(Dispatchers.IO) {
        _discovering.value = true
        _discovered.value = null
        val ip = com.solankizm.ola.data.net.NetworkScanner.discover()
        _discovering.value = false
        if (ip != null) _discovered.value = ip
        else _conn.value = ConnectState.Error("No scooter found on this network. Make sure it joined your hotspot and the screen is awake, or enter the IP manually.")
    }

    fun clearDiscovered() { _discovered.value = null }

    fun disconnect() {
        client?.close(); client = null
        _conn.value = ConnectState.Idle
    }

    override fun onCleared() {
        client?.close()
    }
}
