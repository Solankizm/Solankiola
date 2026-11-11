package com.solankizm.ola.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solankizm.ola.data.net.AppCatalog
import com.solankizm.ola.data.net.CatalogApp
import com.solankizm.ola.vm.AppProgress

@Composable
fun AppsScreen(
    linked: Boolean,
    installed: Set<String>,
    progress: AppProgress,
    accent: Color,
    onInstall: (CatalogApp) -> Unit,
    onLaunch: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onPickApk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp)) {
        Text(
            "Apps",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Install apps onto the scooter screen — CarPlay, a browser, or your own APK. " +
                "Installs over the root link; no ADB-key unlock needed.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.height(10.dp))
        WarnCard()

        if (!linked) {
            Spacer(Modifier.height(14.dp))
            Text(
                "⚠  Not connected. Open the Connect tab and link to your scooter first.",
                color = Color(0xFFFF8A1F),
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(18.dp))
        AppCatalog.apps.forEach { app ->
            CatalogRow(
                app = app,
                installed = installed.contains(app.packageName),
                busy = progress.appId == app.id && !progress.done && !progress.error,
                progress = if (progress.appId == app.id) progress else null,
                accent = accent,
                enabled = linked,
                onInstall = { onInstall(app) },
                onLaunch = { onLaunch(app.packageName) },
                onUninstall = { onUninstall(app.packageName) },
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(10.dp))
        Text("Install your own APK", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Have an Android Auto / music / any APK file? Pick it and it installs on the scooter.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp,
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onPickApk,
            enabled = linked && !(progress.appId == "local" && !progress.done && !progress.error),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
        ) { Text("Pick an APK file") }
        if (progress.appId == "local" && progress.message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            ProgressLine(progress, accent)
        }
    }
}

@Composable
private fun WarnCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FF8A1F)),
        border = BorderStroke(1.dp, Color(0x55FF8A1F)),
    ) {
        Text(
            "These are third-party, closed-source apps downloaded from their own public " +
                "sites — Solankizm can't verify them. Install at your own risk, and only " +
                "on your own scooter on your own hotspot.",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun CatalogRow(
    app: CatalogApp,
    installed: Boolean,
    busy: Boolean,
    progress: AppProgress?,
    accent: Color,
    enabled: Boolean,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    onUninstall: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(0.dp))
                        Text("  • ${app.tag}", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(app.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                    Text(app.sizeText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                if (installed) {
                    OutlinedButton(onClick = onLaunch, enabled = enabled) { Text("Open") }
                    Spacer(Modifier.height(0.dp))
                    OutlinedButton(onClick = onUninstall, enabled = enabled) { Text("Remove") }
                } else {
                    Button(
                        onClick = onInstall,
                        enabled = enabled && !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                    ) { Text(if (busy) "Working…" else "Install") }
                }
            }
            if (progress != null && progress.message.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                ProgressLine(progress, accent)
            }
        }
    }
}

@Composable
private fun ProgressLine(progress: AppProgress, accent: Color) {
    val color = when {
        progress.error -> Color(0xFFFF8A1F)
        progress.done -> Color(0xFF2ED47A)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(progress.message, color = color, fontSize = 13.sp)
    if (!progress.done && !progress.error) {
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.fraction.coerceIn(0f, 1f) },
            color = accent,
            modifier = Modifier.fillMaxWidth().height(6.dp),
        )
    }
}
