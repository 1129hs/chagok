package com.heiseung.chagok.vehicle

import kotlinx.coroutines.flow.StateFlow

enum class Gear { P, R, N, D }

// 앱이 쓰는 차량 값만 모아둔 것
data class VehicleSnapshot(
    val gear: Gear,
    val speedKmh: Float,
    val odometerKm: Double,
    val energyPct: Int,
    val seatPosition: Int,
    val warningCount: Int,
    val doorsClosed: Boolean,
    val windowsClosed: Boolean
)

// 지금은 Fake, 나중에 Vehicle SDK 구현으로 갈아끼움
interface VehicleDataSource {
    val snapshot: StateFlow<VehicleSnapshot>
}
