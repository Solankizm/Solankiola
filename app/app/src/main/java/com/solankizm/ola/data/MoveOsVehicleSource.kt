package com.solankizm.ola.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * The REAL data source: reads live telemetry from the MoveOS vehicle service
 * on the head unit. This is a documented stub until `tools/probe.py` tells us
 * exactly where the data lives on your scooter.
 *
 * Candidate reads (all READ-ONLY), in order of preference:
 *   1. A content provider exposed by com.olaelectric.vehiclerangeservice
 *      (query a content:// URI for speed / soc / range / mode).
 *   2. A sticky broadcast / bound AIDL service the HMI listens to.
 *   3. Android Automotive VHAL (android.car CarPropertyManager) if MoveOS
 *      exposes PERF_VEHICLE_SPEED, EV_BATTERY_LEVEL, etc.
 *   4. Last resort: parse the range service shared_prefs XML (slow, coarse).
 *
 * None of these write to the vehicle. Fill this in after reading probe-report.txt.
 */
class MoveOsVehicleSource : VehicleSource {
    override val name = "MoveOS vehicle service (not yet wired)"

    override val stream: Flow<VehicleData> = flowOf(
        VehicleData(connected = false, demo = false)
    )
}
