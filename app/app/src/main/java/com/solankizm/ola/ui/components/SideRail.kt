package com.solankizm.ola.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.solankizm.ola.vm.Screen

private data class NavItem(
    val screen: Screen,
    val on: ImageVector,
    val off: ImageVector,
)

@Composable
fun SideRail(
    current: Screen,
    accent: Color,
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        NavItem(Screen.DASHBOARD, Icons.Filled.Speed, Icons.Outlined.Speed),
        NavItem(Screen.TRIP, Icons.Filled.Route, Icons.Outlined.Route),
        NavItem(Screen.CHARGING, Icons.Filled.Bolt, Icons.Outlined.Bolt),
        NavItem(Screen.RANGE, Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
        NavItem(Screen.CONNECT, Icons.Filled.Wifi, Icons.Outlined.Wifi),
        NavItem(Screen.APPS, Icons.Filled.Apps, Icons.Outlined.Apps),
        NavItem(Screen.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings),
    )
    Column(
        modifier
            .fillMaxHeight()
            .width(92.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        items.forEach { item ->
            val selected = item.screen == current
            val tint by animateColorAsState(
                if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                tween(250), label = "tint",
            )
            val bg by animateColorAsState(
                if (selected) accent.copy(alpha = 0.14f) else Color.Transparent,
                tween(250), label = "bg",
            )
            Box(
                Modifier
                    .size(56.dp)
                    .background(bg, RoundedCornerShape(18.dp))
                    .clickable { onSelect(item.screen) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) item.on else item.off,
                    contentDescription = item.screen.name,
                    tint = tint,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
