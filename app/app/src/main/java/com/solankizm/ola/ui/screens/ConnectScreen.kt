package com.solankizm.ola.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.solankizm.ola.data.net.ConnectState
import com.solankizm.ola.data.net.DeviceInfo
import com.solankizm.ola.ui.components.StatCard

@Composable
fun ConnectScreen(
    state: ConnectState,
    accent: Color,
    onConnect: (ip: String, scope: Int?) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp)
    ) {
        Text(
            "Connect to scooter",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Join your phone's hotspot on the scooter, then enter the IP shown in " +
                "Hotspot → Connected Devices. Reads live info over MoveOS port 50005.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.height(18.dp))

        var ip by remember { mutableStateOf("") }
        var scope by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                label = { Text("Scooter IP  (e.g. 192.168.43.12)") },
                singleLine = true,
                modifier = Modifier.weight(2f),
            )
            OutlinedTextField(
                value = scope,
                onValueChange = { scope = it },
                label = { Text("Scope (IPv6 only)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))

        val connecting = state is ConnectState.Connecting
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onConnect(ip, scope.toIntOrNull()) },
                enabled = !connecting,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                if (connecting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.width(18.dp).height(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Connecting…")
                } else {
                    Text(if (state is ConnectState.Connected) "Reconnect" else "Connect")
                }
            }
            if (state is ConnectState.Connected) {
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
                OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
            }
        }

        Spacer(Modifier.height(20.dp))

        when (state) {
            is ConnectState.Error -> StatusLine("⚠  ${state.message}", Color(0xFFFF8A1F))
            is ConnectState.Connected -> ConnectedInfo(state.info, accent)
            ConnectState.Connecting -> StatusLine("Reaching the scooter…", accent)
            ConnectState.Idle -> StatusLine(
                "Not connected. On a phone without this link, the dashboard shows demo data.",
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusLine(text: String, color: Color) {
    Text(text, color = color, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun ConnectedInfo(info: DeviceInfo, accent: Color) {
    Column {
        StatusLine("✓  Connected — live device info below.", Color(0xFF2ED47A))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Model", info.model, "", accent, Modifier.weight(1f))
            StatCard("MoveOS", info.moveOs.removePrefix("MoveOS").trim(), "", accent, Modifier.weight(1f))
            StatCard("Android", info.android, "", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("RAM used", "${info.ramUsedMb}", "MB", accent, Modifier.weight(1f))
            StatCard("RAM total", "${info.ramTotalMb}", "MB", accent, Modifier.weight(1f))
            StatCard("Uptime", info.uptime, "", accent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Live speed, battery % and range are not shown yet — their exact source on " +
                "MoveOS is confirmed by running tools/probe.py once, then wired in. " +
                "This screen proves the phone→scooter link works with real data.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}
