package com.solankizm.ola.data

import kotlinx.coroutines.flow.Flow

/**
 * Where live vehicle data comes from. The UI depends only on this interface,
 * so we can swap a simulated source (DemoVehicleSource) for a real one that
 * reads the MoveOS vehicle service, once probe.py tells us where the data lives.
 *
 * Contract: a source only ever READS. No method here changes vehicle state.
 */
interface VehicleSource {
    /** A hot stream of snapshots, roughly 5-10 per second. */
    val stream: Flow<VehicleData>

    /** Human label for the Settings "data source" row. */
    val name: String
}
