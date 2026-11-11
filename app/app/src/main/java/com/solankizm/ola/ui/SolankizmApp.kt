package com.solankizm.ola.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.solankizm.ola.data.Profile
import com.solankizm.ola.data.ProfileStore
import com.solankizm.ola.ui.components.SideRail
import com.solankizm.ola.ui.screens.ChargingScreen
import com.solankizm.ola.data.net.ConnectState
import com.solankizm.ola.ui.screens.AppsScreen
import com.solankizm.ola.ui.screens.ConnectScreen
import com.solankizm.ola.ui.screens.DashboardScreen
import com.solankizm.ola.ui.screens.RangeScreen
import com.solankizm.ola.ui.screens.SettingsScreen
import com.solankizm.ola.ui.screens.TripScreen
import com.solankizm.ola.ui.theme.SolankizmTheme
import com.solankizm.ola.vm.DashboardViewModel
import com.solankizm.ola.vm.Screen

@Composable
fun SolankizmApp(vm: DashboardViewModel = viewModel()) {
    val dark by vm.dark.collectAsStateWithLifecycle()
    SolankizmTheme(dark = dark) {
        val demoV by vm.vehicle.collectAsStateWithLifecycle()
        val liveV by vm.live.collectAsStateWithLifecycle()
        val v = liveV ?: demoV
        val screen by vm.screen.collectAsStateWithLifecycle()
        val connState by vm.conn.collectAsStateWithLifecycle()
        val discovering by vm.discovering.collectAsStateWithLifecycle()
        val discovered by vm.discovered.collectAsStateWithLifecycle()
        val installed by vm.installed.collectAsStateWithLifecycle()
        val appProgress by vm.appProgress.collectAsStateWithLifecycle()
        val accent = v.rideMode.accent

        // ---- Rider profile (photo + name), on-device only -----------------
        val context = LocalContext.current
        val store = remember { ProfileStore(context) }
        var profile by remember { mutableStateOf(store.load()) }

        val photoPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val bmp = store.savePhoto(uri)
                profile = profile.copy(photo = bmp)
            }
        }

        val apkPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri -> if (uri != null) vm.installLocalApk(context, uri) }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background,
                        1f to Color.Black.copy(alpha = if (dark) 0.6f else 0f),
                    )
                )
        ) {
            Row(Modifier.fillMaxSize()) {
                SideRail(current = screen, accent = accent, onSelect = vm::go)
                Box(Modifier.fillMaxSize().padding(start = 4.dp)) {
                    when (screen) {
                        Screen.DASHBOARD -> DashboardScreen(v, profile)
                        Screen.TRIP -> TripScreen(v, accent)
                        Screen.CHARGING -> ChargingScreen(v, accent)
                        Screen.RANGE -> RangeScreen(v, accent)
                        Screen.CONNECT -> ConnectScreen(
                            state = connState,
                            accent = accent,
                            discovering = discovering,
                            discovered = discovered,
                            onScan = { vm.autoDiscover() },
                            onClearDiscovered = { vm.clearDiscovered() },
                            onConnect = { ip, s -> vm.connect(ip, s) },
                            onRefresh = { vm.refreshInfo() },
                            onDisconnect = { vm.disconnect() },
                        )
                        Screen.APPS -> AppsScreen(
                            linked = connState is ConnectState.Connected,
                            installed = installed,
                            progress = appProgress,
                            accent = accent,
                            onInstall = { vm.installCatalog(context, it) },
                            onLaunch = { vm.launchPkg(it) },
                            onUninstall = { vm.uninstallPkg(it) },
                            onPickApk = { apkPicker.launch("application/vnd.android.package-archive") },
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            sourceName = vm.sourceName,
                            dark = dark,
                            onToggleTheme = { vm.toggleTheme() },
                            profile = profile,
                            accent = accent,
                            onNameChange = {
                                store.saveName(it)
                                profile = profile.copy(name = it.ifBlank { "Rider" })
                            },
                            onPickPhoto = {
                                photoPicker.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
