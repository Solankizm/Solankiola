package com.solankizm.ola.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solankizm.ola.data.GearState
import com.solankizm.ola.data.VehicleData

/**
 * A compact pill-shaped status strip at the top — the Solankizm take on the
 * v1.3 "Dynamic Island". Shows gear, live indicators and charging at a glance.
 */
@Composable
fun DynamicIsland(v: VehicleData, accent: Color, modifier: Modifier = Modifier) {
    val blink = rememberInfiniteTransition(label = "blink")
    val a by blink.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "a",
    )
    Row(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = "left",
            tint = if (v.leftIndicator) Color(0xFF2ED47A) else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp).alpha(if (v.leftIndicator) a else 0.35f),
        )
        Text(
            gearLabel(v.gear),
            color = accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        if (v.isCharging) {
            Icon(
                Icons.Filled.BatteryChargingFull,
                contentDescription = "charging",
                tint = Color(0xFF2ED47A),
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            "${v.socPercent}%",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            Icons.Filled.ArrowForward,
            contentDescription = "right",
            tint = if (v.rightIndicator) Color(0xFF2ED47A) else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp).alpha(if (v.rightIndicator) a else 0.35f),
        )
    }
}

private fun gearLabel(g: GearState) = when (g) {
    GearState.PARK -> "P"
    GearState.DRIVE -> "D"
    GearState.REVERSE -> "R"
    GearState.NEUTRAL -> "N"
}
