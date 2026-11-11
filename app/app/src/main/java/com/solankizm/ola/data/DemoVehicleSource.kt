package com.solankizm.ola.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A believable simulated ride, so the whole app can be built, demoed and
 * reviewed with no scooter attached. It accelerates, cruises, brakes with a
 * little regen, drains the battery slowly and cycles ride modes.
 */
class DemoVehicleSource : VehicleSource {
    override val name = "Demo (simulated ride)"

    override val stream: Flow<VehicleData> = flow {
        var t = 0.0
        var soc = 86.0
        var odo = 4123
        var trip = 12.4
        val modes = RideMode.values()
        var modeIdx = 1

        while (true) {
            // a smooth speed curve 0..topSpeed using layered sines
            val mode = modes[modeIdx]
            val base = (sin(t / 7.0) * 0.5 + 0.5)          // 0..1 slow wave
            val jitter = sin(t / 1.3) * 0.04
            val speed = max(0.0, (base + jitter) * mode.topSpeed)
            val moving = speed > 1

            // power: positive when accelerating, slight regen when slowing
            val accel = sin(t / 7.0 + 0.3)
            val power = accel * (mode.topSpeed / 18.0)

            if (moving) {
                val dist = speed / 3600.0 * 0.4   // km in a 0.4s tick
                odo += 0            // keep odo integer-stable; add fractional to trip
                trip += dist
                soc = max(0.0, soc - dist * 1.1)  // ~1.1%/km-ish demo drain
            }

            val range = (soc / 100.0 * mode.topSpeed * 1.9).roundToInt()

            emit(
                VehicleData(
                    speedKmph = speed.roundToInt(),
                    socPercent = soc.roundToInt(),
                    rangeKm = range,
                    rideMode = mode,
                    gear = if (moving) GearState.DRIVE else GearState.PARK,
                    odometerKm = odo + (trip).toInt(),
                    tripKm = (trip * 10).roundToInt() / 10.0,
                    motorTempC = (34 + base * 22 + modeIdx * 3).roundToInt(),
                    isCharging = false,
                    leftIndicator = (t % 12.0) < 1.2,
                    rightIndicator = (t % 20.0) in 0.0..1.2,
                    powerKw = (power * 10).roundToInt() / 10.0,
                    connected = true,
                    demo = true,
                )
            )

            // change ride mode every ~18s
            if ((t % 18.0) < 0.4) modeIdx = (modeIdx + 1) % modes.size
            t += 0.4
            delay(400)
        }
    }
}
