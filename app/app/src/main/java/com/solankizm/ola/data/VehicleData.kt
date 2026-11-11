package com.solankizm.ola.data

import androidx.compose.ui.graphics.Color
import com.solankizm.ola.ui.theme.Brand

/** Ride modes as shown on MoveOS. Each carries the accent colour used across the UI. */
enum class RideMode(val label: String, val accent: Color, val topSpeed: Int) {
    ECO("ECO", Brand.Eco, 45),
    NORMAL("NORMAL", Brand.Normal, 75),
    SPORTS("SPORTS", Brand.Sports, 95),
    HYPER("HYPER", Brand.Hyper, 125),
}

enum class GearState { PARK, DRIVE, REVERSE, NEUTRAL }

/**
 * A single immutable snapshot of what the scooter is doing right now.
 * Everything the UI renders comes from one of these. Read-only by design:
 * there is no field here that writes back to the vehicle.
 */
data class VehicleData(
    val speedKmph: Int = 0,
    val socPercent: Int = 0,          // state of charge 0..100
    val rangeKm: Int = 0,             // estimated remaining range for current mode
    val rideMode: RideMode = RideMode.NORMAL,
    val gear: GearState = GearState.PARK,
    val odometerKm: Int = 0,
    val tripKm: Double = 0.0,
    val motorTempC: Int = 0,
    val isCharging: Boolean = false,
    val leftIndicator: Boolean = false,
    val rightIndicator: Boolean = false,
    val powerKw: Double = 0.0,         // instantaneous power; negative = regen
    val connected: Boolean = false,    // live link to the vehicle service?
    val demo: Boolean = false,         // true when values are simulated
) {
    val batteryBars: Int get() = (socPercent + 9) / 10   // 0..10 LED segments
}
