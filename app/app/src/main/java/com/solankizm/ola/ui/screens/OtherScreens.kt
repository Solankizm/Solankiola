package com.solankizm.ola.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solankizm.ola.data.Profile
import com.solankizm.ola.data.VehicleData
import com.solankizm.ola.ui.components.Avatar
import com.solankizm.ola.ui.components.StatCard

@Composable
private fun Header(title: String) {
    Text(
        title,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun TripScreen(v: VehicleData, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(28.dp)) {
        Header("Trip")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("This trip", "%.1f".format(v.tripKm), "km", accent, Modifier.weight(1f))
            StatCard("Odometer", "${v.odometerKm}", "km", accent, Modifier.weight(1f))
            StatCard("Avg power", "%.1f".format(v.powerKw), "kW", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("Mode", v.rideMode.label, "", accent, Modifier.weight(1f))
            StatCard("Top (mode)", "${v.rideMode.topSpeed}", "km/h", accent, Modifier.weight(1f))
            StatCard("Motor temp", "${v.motorTempC}", "°C", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Note("Trip history is logged on-device. Charts arrive once live telemetry is wired from your scooter (run tools/probe.py).")
    }
}

@Composable
fun ChargingScreen(v: VehicleData, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(28.dp)) {
        Header("Charging")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("Battery", "${v.socPercent}", "%", accent, Modifier.weight(1f))
            StatCard("Status", if (v.isCharging) "Charging" else "Idle", "", accent, Modifier.weight(1f))
            StatCard("Range now", "${v.rangeKm}", "km", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Note("Charge reminder at 80% and battery-health trend will show here. Solankizm never controls charging — it only reads the level.")
    }
}

@Composable
fun RangeScreen(v: VehicleData, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(28.dp)) {
        Header("Range planner")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("Est. range", "${v.rangeKm}", "km", accent, Modifier.weight(1f))
            StatCard("There & back", "${v.rangeKm / 2}", "km", accent, Modifier.weight(1f))
            StatCard("Battery", "${v.socPercent}", "%", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Note("Range is an estimate for the current ride mode (${v.rideMode.label}). Real conditions vary with load, weather and terrain — treat it as a guide, not a guarantee.")
    }
}

@Composable
fun SettingsScreen(
    sourceName: String,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    profile: Profile,
    accent: Color,
    onNameChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(28.dp)) {
        Header("Settings")
        Spacer(Modifier.height(20.dp))

        // ---- Rider profile -------------------------------------------------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile = profile, accent = accent, size = 72.dp)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                var name by remember(profile.name) { mutableStateOf(profile.name) }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; onNameChange(it) },
                    label = { Text("Your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.7f),
                )
            }
            Button(
                onClick = onPickPhoto,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (profile.photo == null) "Add photo" else "Change photo")
            }
        }
        Spacer(Modifier.height(10.dp))
        Note("Your photo and name stay on this device. You can also push a photo from your Mac: app/set-photo.sh <ip> <photo.jpg>.")

        Spacer(Modifier.height(22.dp))
        SettingRow("Dark theme", if (dark) "On" else "Off") {
            Switch(checked = dark, onCheckedChange = { onToggleTheme() })
        }
        Spacer(Modifier.height(12.dp))
        LabeledRow("Data source", sourceName)
        LabeledRow("App", "Solankizm for Ola S1 · v0.1.0")
        LabeledRow("Mode", "Read-only — never controls the vehicle")
        Spacer(Modifier.height(20.dp))
        Note("Solankizm is an independent project and is not affiliated with Ola Electric. \"Ola\" is a trademark of its owner.")
    }
}

@Composable
private fun SettingRow(label: String, value: String, trailing: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        trailing()
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, modifier = Modifier.weight(0.4f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    )
}
