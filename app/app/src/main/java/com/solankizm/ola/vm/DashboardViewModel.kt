package com.solankizm.ola.vm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solankizm.ola.data.DemoVehicleSource
import com.solankizm.ola.data.VehicleData
import com.solankizm.ola.data.VehicleSource
import com.solankizm.ola.data.net.AppInstaller
import com.solankizm.ola.data.net.CatalogApp
import com.solankizm.ola.data.net.ConnectState
import com.solankizm.ola.data.net.DataAnalyser
import com.solankizm.ola.data.net.NetworkScanner
import com.solankizm.ola.data.net.S1Client
import com.solankizm.ola.data.net.readDeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { DASHBOARD, TRIP, CHARGING, RANGE, CONNECT, APPS, SETTINGS }

/** Progress for one App-Manager action (download/install). */
data class AppProgress(
    val appId: String? = null,
    val message: String = "",
    val fraction: Float = 0f,
    val done: Boolean = false,
    val error: Boolean = false,
)

class DashboardViewModel @JvmOverloads constructor(
    private val source: VehicleSource = DemoVehicleSource(),
) : ViewModel() {

    private val demo: StateFlow<VehicleData> =
        source.stream.stateIn(viewModelScope, SharingStarted.Eagerly, VehicleData())

    // Live telemetry read from the scooter when connected; null = use demo.
    private val _live = MutableStateFlow<VehicleData?>(null)
    val live: StateFlow<VehicleData?> = _live.asStateFlow()

    /** What the dashboard shows: real data when linked, otherwise the demo ride. */
    val vehicle: StateFlow<VehicleData> get() = demo

    private val _screen = MutableStateFlow(Screen.DASHBOARD)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _dark = MutableStateFlow(true)
    val dark: StateFlow<Boolean> = _dark.asStateFlow()

    // ---- Phone -> scooter live link -----------------------------------------
    private val _conn = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val conn: StateFlow<ConnectState> = _conn.asStateFlow()
    private var client: S1Client? = null
    private var pollJob: Job? = null

    private val _discovering = MutableStateFlow(false)
    val discovering: StateFlow<Boolean> = _discovering.asStateFlow()
    private val _discovered = MutableStateFlow<String?>(null)
    val discovered: StateFlow<String?> = _discovered.asStateFlow()

    // ---- App Manager --------------------------------------------------------
    private val _installed = MutableStateFlow<Set<String>>(emptySet())
    val installed: StateFlow<Set<String>> = _installed.asStateFlow()
    private val _appProgress = MutableStateFlow(AppProgress())
    val appProgress: StateFlow<AppProgress> = _appProgress.asStateFlow()

    val isLinked: Boolean get() = client != null
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
            runCatching { _installed.value = AppInstaller.installedPackages(c) }
            startLivePolling(c)
        } catch (e: Exception) {
            client?.close(); client = null
            _conn.value = ConnectState.Error(e.message ?: "Could not reach the scooter.")
        }
    }

    /** Poll the vehicle-range-service prefs for real battery/odo/trip/mode. */
    private fun startLivePolling(c: S1Client) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val xml = runCatching { c.shell("cat ${DataAnalyser.PATH}", 12L).second }.getOrNull()
                if (!xml.isNullOrBlank() && xml.contains("CURRENT_BATTERY_SOC")) {
                    runCatching { _live.value = DataAnalyser.parse(xml) }
                }
                delay(2500)
            }
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
        val ip = NetworkScanner.discover()
        _discovering.value = false
        if (ip != null) _discovered.value = ip
        else _conn.value = ConnectState.Error(
            "No scooter found on this network. Make sure it joined your hotspot and the screen is awake, or enter the IP manually."
        )
    }

    fun clearDiscovered() { _discovered.value = null }

    // ---- App Manager actions ------------------------------------------------
    fun refreshInstalled() = viewModelScope.launch(Dispatchers.IO) {
        val c = client ?: return@launch
        runCatching { _installed.value = AppInstaller.installedPackages(c) }
    }

    fun installCatalog(context: Context, app: CatalogApp) = viewModelScope.launch(Dispatchers.IO) {
        val c = client ?: run {
            _appProgress.value = AppProgress(app.id, "Connect to the scooter first (Connect tab).", error = true)
            return@launch
        }
        try {
            _appProgress.value = AppProgress(app.id, "Downloading ${app.name}…", 0f)
            val file = AppInstaller.downloadToCache(context, app.url, "${app.id}.apk") { f ->
                _appProgress.value = AppProgress(app.id, "Downloading…", f)
            }
            AppInstaller.install(c, file, app.packageName, app.needsOverlay) { f ->
                _appProgress.value = AppProgress(app.id, "Installing on scooter…", f)
            }
            _appProgress.value = AppProgress(app.id, "Installed ✓", 1f, done = true)
            runCatching { _installed.value = AppInstaller.installedPackages(c) }
        } catch (e: Exception) {
            _appProgress.value = AppProgress(app.id, e.message ?: "Install failed", 0f, error = true)
        }
    }

    fun installLocalApk(context: Context, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val c = client ?: run {
            _appProgress.value = AppProgress("local", "Connect to the scooter first (Connect tab).", error = true)
            return@launch
        }
        try {
            _appProgress.value = AppProgress("local", "Reading APK…", 0.2f)
            val file = AppInstaller.copyUriToCache(context, uri)
            AppInstaller.install(c, file, "", false) { f ->
                _appProgress.value = AppProgress("local", "Installing on scooter…", f)
            }
            _appProgress.value = AppProgress("local", "Installed ✓", 1f, done = true)
            runCatching { _installed.value = AppInstaller.installedPackages(c) }
        } catch (e: Exception) {
            _appProgress.value = AppProgress("local", e.message ?: "Install failed", 0f, error = true)
        }
    }

    fun launchPkg(pkg: String) = viewModelScope.launch(Dispatchers.IO) {
        client?.let { AppInstaller.launch(it, pkg) }
    }

    fun uninstallPkg(pkg: String) = viewModelScope.launch(Dispatchers.IO) {
        val c = client ?: return@launch
        runCatching { AppInstaller.uninstall(c, pkg); _installed.value = AppInstaller.installedPackages(c) }
    }

    fun disconnect() {
        pollJob?.cancel(); pollJob = null
        client?.close(); client = null
        _conn.value = ConnectState.Idle
        _installed.value = emptySet()
        _live.value = null
    }

    override fun onCleared() {
        pollJob?.cancel()
        client?.close()
    }
}
