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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.solankizm.ola.ui.components.BatteryLeds
import com.solankizm.ola.ui.components.DynamicIsland
import com.solankizm.ola.ui.components.SpeedArc
import com.solankizm.ola.ui.components.StatCard

@Composable
fun DashboardScreen(v: VehicleData, profile: Profile, modifier: Modifier = Modifier) {
    val accent by animateColorAsState(v.rideMode.accent, tween(500), label = "accent")
    val fraction = v.speedKmph.toFloat() / v.rideMode.topSpeed.coerceAtLeast(1)

    Row(modifier.fillMaxSize().padding(24.dp)) {
        // ---- Left: battery column -----------------------------------------
        Column(
            Modifier.fillMaxWidth(0.12f).padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${v.socPercent}%",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "BATTERY",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(14.dp))
            BatteryLeds(
                bars = v.batteryBars,
                accent = accent,
                track = MaterialTheme.colorScheme.outline,
                modifier = Modifier.width(34.dp).height(260.dp),
            )
        }

        // ---- Center: speedometer ------------------------------------------
        Column(
            Modifier.fillMaxWidth(0.62f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(profile = profile, accent = accent, size = 40.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Hi, ${profile.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            DynamicIsland(v, accent)
            Spacer(Modifier.height(8.dp))
            Box(contentAlignment = Alignment.Center) {
                SpeedArc(
                    fraction = fraction,
                    accent = accent,
                    track = MaterialTheme.colorScheme.outline,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${v.speedKmph}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 130.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "km/h",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(10.dp))
                    RideModePill(v, accent)
                }
            }
        }

        // ---- Right: live stat stack ---------------------------------------
        Column(
            Modifier.fillMaxWidth().padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatCard("Range", "${v.rangeKm}", "km", accent, Modifier.fillMaxWidth())
            StatCard("Trip", "%.1f".format(v.tripKm), "km", accent, Modifier.fillMaxWidth())
            StatCard("Odometer", "${v.odometerKm}", "km", accent, Modifier.fillMaxWidth())
            StatCard("Motor", "${v.motorTempC}", "°C", accent, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RideModePill(v: VehicleData, accent: Color) {
    Row(
        Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(50))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 22.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).background(accent, RoundedCornerShape(50)))
        Spacer(Modifier.width(10.dp))
        Text(
            v.rideMode.label,
            color = accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        if (v.demo) {
            Spacer(Modifier.width(10.dp))
            Text(
                "· DEMO",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
