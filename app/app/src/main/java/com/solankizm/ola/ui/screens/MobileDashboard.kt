package com.solankizm.ola.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solankizm.ola.data.Profile
import com.solankizm.ola.data.VehicleData
import com.solankizm.ola.ui.components.Avatar
import com.solankizm.ola.ui.components.RingGauge
import com.solankizm.ola.ui.components.StatCard

/**
 * Phone (portrait) dashboard — Ola/MoveOS-style: a big battery/range ring hero
 * and a grid of rounded stat cards. Scrollable, one column. Speed is shown as a
 * tile here (a phone is usually used parked); the scooter screen keeps the big
 * speedometer.
 */
@Composable
fun MobileDashboard(v: VehicleData, profile: Profile, modifier: Modifier = Modifier) {
    val accent by animateColorAsState(v.rideMode.accent, tween(500), label = "accent")

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        // ---- greeting + status ------------------------------------------------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile = profile, accent = accent, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Hi, ${profile.name}", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (v.connected) "Ola S1 Pro · ${if (v.isCharging) "Charging" else "Parked"}" else "Not connected — demo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                )
            }
            StatusChip(v, accent)
        }

        Spacer(Modifier.height(22.dp))

        // ---- battery / range hero --------------------------------------------
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            RingGauge(
                fraction = v.socPercent / 100f,
                accent = accent,
                track = MaterialTheme.colorScheme.outline,
                size = 230.dp,
                stroke = 18.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${v.socPercent}", color = MaterialTheme.colorScheme.onSurface, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                        Text("%", color = accent, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                    }
                    if (v.isCharging) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BatteryChargingFull, null, tint = Color(0xFF2ED47A), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Charging", color = Color(0xFF2ED47A), fontSize = 13.sp)
                        }
                    } else {
                        Text("BATTERY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${v.rangeKm} km range", color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { ModePill(v, accent) }
        Spacer(Modifier.height(20.dp))

        // ---- stat grid (2 columns) -------------------------------------------
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Speed", "${v.speedKmph}", "km/h", accent, Modifier.weight(1f))
            StatCard("Range", "${v.rangeKm}", "km", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Trip", "%.1f".format(v.tripKm), "km", accent, Modifier.weight(1f))
            StatCard("Odometer", "${v.odometerKm}", "km", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Ride mode", v.rideMode.label, "", accent, Modifier.weight(1f))
            StatCard("Status", if (v.isCharging) "Charging" else "Parked", "", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        if (v.demo) {
            Text(
                "Showing demo data. Open Connect and link to your scooter to see live battery, range and odometer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp,
            )
        } else {
            Text(
                "Live from your scooter. Instantaneous speed arrives after the live-speed probe.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun StatusChip(v: VehicleData, accent: Color) {
    val (label, color) = if (v.demo) "DEMO" to MaterialTheme.colorScheme.onSurfaceVariant else "LIVE" to Color(0xFF2ED47A)
    Row(
        Modifier.background(color.copy(alpha = 0.14f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModePill(v: VehicleData, accent: Color) {
    Row(
        Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(50))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(accent, RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text(v.rideMode.label, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    }
}
