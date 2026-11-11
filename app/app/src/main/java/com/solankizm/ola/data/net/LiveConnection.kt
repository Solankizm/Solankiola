package com.solankizm.ola.data.net

/** Real, verified facts we can read from the scooter today (no probe needed). */
data class DeviceInfo(
    val model: String = "—",
    val moveOs: String = "—",
    val android: String = "—",
    val ramUsedMb: Int = 0,
    val ramTotalMb: Int = 0,
    val uptime: String = "—",
    val scooterMode: String = "—",
)

sealed interface ConnectState {
    data object Idle : ConnectState
    data object Connecting : ConnectState
    data class Connected(val info: DeviceInfo) : ConnectState
    data class Error(val message: String) : ConnectState
}

/**
 * Reads only facts we are confident about (these exact commands are the ones
 * ola.py uses). Live telemetry — speed, state-of-charge, range, ride mode —
 * is deliberately NOT guessed here; its location is confirmed by tools/probe.py
 * and wired into MoveOsVehicleSource afterwards.
 */
suspend fun readDeviceInfo(client: S1Client): DeviceInfo {
    suspend fun one(cmd: String) = runCatching { client.shell(cmd, 10L).second.trim() }.getOrDefault("")

    val modelRaw = one("getprop ro.product.model")
    val model = when {
        modelRaw.contains("S1 Pro", true) -> "Ola S1 Pro"
        modelRaw.contains("S1 Air", true) -> "Ola S1 Air"
        modelRaw.contains("S1 X", true) -> "Ola S1 X"
        modelRaw.contains("S1", true) -> "Ola S1"
        modelRaw.isNotBlank() -> modelRaw
        else -> "Ola S1"
    }

    val swRaw = runCatching { client.getSwVersion() }.getOrDefault("").trim()
    val firstLine = swRaw.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
        ?: one("getprop ro.build.display.id")
    val moveOs = when {
        firstLine.isBlank() -> "—"
        firstLine.startsWith("MoveOS", true) -> firstLine
        else -> "MoveOS $firstLine"
    }

    val android = one("getprop ro.build.version.release")
    val mode = one("getprop persist.ola.security.scootermode").ifBlank { "—" }
    val uptime = one("uptime").substringAfter("up ", "").substringBefore(",").trim().ifBlank { "—" }

    var totalKb = 0L; var availKb = 0L
    one("cat /proc/meminfo").lineSequence().forEach { line ->
        val parts = line.split(":")
        if (parts.size >= 2) {
            val v = parts[1].trim().split(Regex("\\s+")).firstOrNull()?.toLongOrNull() ?: 0L
            when (parts[0].trim()) {
                "MemTotal" -> totalKb = v
                "MemAvailable" -> availKb = v
            }
        }
    }
    val totalMb = (totalKb / 1024).toInt()
    val usedMb = ((totalKb - availKb) / 1024).toInt().coerceAtLeast(0)

    return DeviceInfo(
        model = model,
        moveOs = moveOs,
        android = android.ifBlank { "—" },
        ramUsedMb = usedMb,
        ramTotalMb = totalMb,
        uptime = uptime,
        scooterMode = mode,
    )
}
