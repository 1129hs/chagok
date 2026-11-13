package com.heiseung.chagok.vehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.max

// SDK 연결 전 화면 흐름 테스트용 가짜 차
class FakeVehicleDataSource : VehicleDataSource, SeatTestControl {

    private val _snapshot = MutableStateFlow(
        VehicleSnapshot(
            gear = Gear.P,
            speedKmh = 0f,
            odometerKm = 12034.5,
            energyPct = 67,
            seatPosition = 32,
            warningCount = 0,
            doorsClosed = true,
            windowsClosed = true
        )
    )
    override val snapshot: StateFlow<VehicleSnapshot> = _snapshot.asStateFlow()

    override fun setSeatPosition(value: Int) = _snapshot.update { it.copy(seatPosition = value.coerceIn(0, 100)) }

    // 출발: P → D
    fun drive() = _snapshot.update { it.copy(gear = Gear.D, speedKmh = 40f) }

    // 도착: 주행거리·배터리 바꾸고 D → P
    fun park(addKm: Double = 12.4, useEnergyPct: Int = 6) = _snapshot.update {
        it.copy(
            gear = Gear.P,
            speedKmh = 0f,
            odometerKm = it.odometerKm + addKm,
            energyPct = max(0, it.energyPct - useEnergyPct)
        )
    }
}
