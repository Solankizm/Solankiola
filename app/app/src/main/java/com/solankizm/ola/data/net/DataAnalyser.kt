package com.solankizm.ola.data.net

import com.solankizm.ola.data.GearState
import com.solankizm.ola.data.RideMode
import com.solankizm.ola.data.VehicleData
import kotlin.math.roundToInt

/**
 * Parses MoveOS's live telemetry out of the vehicle-range-service prefs file
 *   /data/data/com.olaelectric.vehiclerangeservice/shared_prefs/com.olaelectric.dataanalyser.xml
 * confirmed by tools/probe.py on an S1 Pro Gen2. Real battery %, odometer, trip,
 * ride mode and charging come from here. (Instantaneous speed is not persisted
 * here — see the live-speed probe note.)
 */
object DataAnalyser {

    const val PATH =
        "/data/data/com.olaelectric.vehiclerangeservice/shared_prefs/com.olaelectric.dataanalyser.xml"

    private const val P = "com.olaelectric.dataanalyser"

    /** value from a numeric/boolean entry: <int name="P.KEY" value="123" /> */
    private fun attr(xml: String, key: String): String? =
        Regex("""name="$P\.$key"\s+value="([^"]*)"""").find(xml)?.groupValues?.get(1)

    /** text from a string entry: <string name="P.KEY">text</string> */
    private fun text(xml: String, key: String): String? =
        Regex("""name="$P\.$key"\s*>([^<]*)<""").find(xml)?.groupValues?.get(1)

    fun modelName(xml: String): String {
        val raw = text(xml, "VEHICLE_VARIANT")?.replace("_", " ")?.trim().orEmpty()
        return when {
            raw.contains("S1 Pro", true) -> if (raw.contains("Gen2", true) || raw.contains("Gen 2", true)) "Ola S1 Pro Gen 2" else "Ola S1 Pro"
            raw.contains("S1 Air", true) -> "Ola S1 Air"
            raw.contains("S1 X", true) -> "Ola S1 X"
            raw.contains("S1", true) -> "Ola S1"
            raw.isNotBlank() -> raw
            else -> "Ola S1"
        }
    }

    fun parse(xml: String): VehicleData {
        val soc = attr(xml, "CURRENT_BATTERY_SOC")?.toFloatOrNull()?.roundToInt()?.coerceIn(0, 100) ?: 0
        val odoKm = attr(xml, "ODOMETER_READING")?.toIntOrNull()
            ?: (attr(xml, "ODOMETER_READING_IN_METER")?.toLongOrNull()?.div(1000))?.toInt() ?: 0
        val tripKm = ((attr(xml, "TRIP_DISTANCE_PMS")?.toDoubleOrNull() ?: 0.0) / 1000.0)
        val charging = attr(xml, "CHARGING_FLAG") == "true"
        val topRide = attr(xml, "TOP_SPEED_CURRENT_RIDE")?.toIntOrNull() ?: 0
        val avg = attr(xml, "AVG_SPEED__CURRENT_RIDE")?.toIntOrNull() ?: 0

        val mode = when {
            attr(xml, "HYPER_MODE") == "true" -> RideMode.HYPER
            attr(xml, "SPORT_MODE") == "true" -> RideMode.SPORTS
            attr(xml, "ECO_MODE") == "true" -> RideMode.ECO
            else -> RideMode.NORMAL
        }

        // No dedicated range field in this file — estimate from SOC (labelled as
        // an estimate in the UI). Rough real-world factor for S1 Pro Gen2.
        val rangeKm = (soc / 100.0 * 150).roundToInt()

        // Instantaneous speed isn't persisted here; show 0 (parked) until the
        // live-speed source is wired. avg/top are available via trip screen.
        return VehicleData(
            speedKmph = 0,
            socPercent = soc,
            rangeKm = rangeKm,
            rideMode = mode,
            gear = GearState.PARK,
            odometerKm = odoKm,
            tripKm = (tripKm * 10).roundToInt() / 10.0,
            motorTempC = 0,
            isCharging = charging,
            powerKw = 0.0,
            connected = true,
            demo = false,
        )
    }
}
