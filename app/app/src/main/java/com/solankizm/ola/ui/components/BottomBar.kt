package com.solankizm.ola.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.solankizm.ola.vm.Screen

private data class BarItem(val screen: Screen, val label: String, val on: ImageVector, val off: ImageVector)

/**
 * Phone bottom navigation (5 primary destinations). Charging and Range info is
 * on the dashboard hero, so they are not separate tabs on mobile.
 */
@Composable
fun BottomBar(current: Screen, accent: Color, onSelect: (Screen) -> Unit) {
    val items = listOf(
        BarItem(Screen.DASHBOARD, "Home", Icons.Filled.Speed, Icons.Outlined.Speed),
        BarItem(Screen.CONNECT, "Connect", Icons.Filled.Wifi, Icons.Outlined.Wifi),
        BarItem(Screen.APPS, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
        BarItem(Screen.TRIP, "Trip", Icons.Filled.Route, Icons.Outlined.Route),
        BarItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = item.screen == current
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item.screen) },
                icon = { Icon(if (selected) item.on else item.off, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = accent.copy(alpha = 0.16f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
