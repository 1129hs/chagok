package com.heiseung.chagok.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.constant.area.DoorValue
import ai.pleos.playground.vehicle.constant.area.SeatValue
import ai.pleos.playground.vehicle.constant.area.WindowValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.DoorPositionListener
import ai.pleos.playground.vehicle.listener.EvBatteryStateOfChargeListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.SeatForeAftPositionListener
import ai.pleos.playground.vehicle.listener.WindowPositionListener
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Pleos Vehicle SDK로 실제 차량 값 구독 (에뮬레이터에선 Virtual Console로 바꿈)
@Suppress("DEPRECATION")
class SdkVehicleDataSource(context: Context) : VehicleDataSource, SeatTestControl {

    private val vehicle = Vehicle(context.applicationContext)

    private val _snapshot = MutableStateFlow(
        VehicleSnapshot(
            gear = Gear.P,
            speedKmh = 0f,
            odometerKm = 0.0,
            energyPct = 100,
            seatPosition = 0,
            warningCount = 0,
            doorsClosed = true,
            windowsClosed = true
        )
    )
    override val snapshot: StateFlow<VehicleSnapshot> = _snapshot.asStateFlow()

    // 문·창문은 하나라도 열리면 "열림"
    private val openDoors = mutableSetOf<DoorValue>()
    private val openWindows = mutableSetOf<WindowValue>()

    fun start() {
        try {
            vehicle.initialize()
            subscribe()
        } catch (e: Exception) {
            Log.e(TAG, "Vehicle SDK 시작 실패", e)
        }
    }

    fun release() {
        runCatching { vehicle.release() }
    }

    // 시연용: Virtual Console에 시트 위치가 없어서 화면 버튼으로 바꿈 (다음 SDK 값이 오면 그 값으로)
    override fun setSeatPosition(value: Int) = _snapshot.update { it.copy(seatPosition = value.coerceIn(0, 100)) }

    private fun subscribe() {
        vehicle.getGear().registerGearSelection(object : GearSelectionListener {
            override fun onSelected(gear: VehicleGear?) {
                val mapped = gear?.toGear() ?: return
                Log.d(TAG, "기어 $gear")
                _snapshot.update { it.copy(gear = mapped, speedKmh = if (mapped == Gear.P) 0f else it.speedKmh) }
            }
            override fun onFailed(e: Exception) = fail("기어", e)
        })

        vehicle.getOdometer().registerDistanceDriven(object : DistanceDrivenListener {
            override fun onDistanceDrivenUpdated(value: Float?) {
                value ?: return
                _snapshot.update { it.copy(odometerKm = value.toDouble()) }
            }
            override fun onFailed(e: Exception) = fail("주행거리", e)
        })

        vehicle.getEvBattery().registerEvBatteryStateOfCharge(object : EvBatteryStateOfChargeListener {
            override fun onEvBatteryStateOfChargeUpdated(value: Int?) {
                value ?: return
                _snapshot.update { it.copy(energyPct = value.coerceIn(0, 100)) }
            }
            override fun onFailed(e: Exception) = fail("배터리", e)
        })

        vehicle.getSeat().registerSeatForeAftPosition(
            SeatArea(SeatValue.ROW_1_LEFT),
            object : SeatForeAftPositionListener {
                override fun onSeatForeAftPositionUpdated(position: Int?) {
                    position ?: return
                    _snapshot.update { it.copy(seatPosition = position.coerceIn(0, 100)) }
                }
                override fun onFailed(e: Exception) = fail("시트", e)
            }
        )

        // 앞뒤 문 4개 + 트렁크
        listOf(
            DoorValue.ROW_1_LEFT, DoorValue.ROW_1_RIGHT,
            DoorValue.ROW_2_LEFT, DoorValue.ROW_2_RIGHT, DoorValue.REAR
        ).forEach { door ->
            vehicle.getDoor().registerDoorPosition(DoorArea(door), object : DoorPositionListener {
                override fun onPositionUpdated(pos: Int?) {
                    pos ?: return
                    synchronized(openDoors) {
                        if (pos > 0) openDoors.add(door) else openDoors.remove(door)
                        _snapshot.update { it.copy(doorsClosed = openDoors.isEmpty()) }
                    }
                }
                override fun onFailed(e: Exception) = fail("문 $door", e)
            })
        }

        listOf(
            WindowValue.ROW_1_LEFT, WindowValue.ROW_1_RIGHT,
            WindowValue.ROW_2_LEFT, WindowValue.ROW_2_RIGHT
        ).forEach { window ->
            vehicle.getWindow().registerWindowPosition(WindowArea(window), object : WindowPositionListener {
                override fun onWindowPositionUpdated(position: Int?) {
                    position ?: return
                    synchronized(openWindows) {
                        if (position > 0) openWindows.add(window) else openWindows.remove(window)
                        _snapshot.update { it.copy(windowsClosed = openWindows.isEmpty()) }
                    }
                }
                override fun onFailed(e: Exception) = fail("창문 $window", e)
            })
        }
    }

    private fun fail(what: String, e: Exception) {
        Log.w(TAG, "$what 구독 실패: ${e.message}")
    }

    private fun VehicleGear.toGear(): Gear? = when (this) {
        VehicleGear.GEAR_UNKNOWN -> null
        VehicleGear.GEAR_PARK -> Gear.P
        VehicleGear.GEAR_REVERSE -> Gear.R
        VehicleGear.GEAR_NEUTRAL -> Gear.N
        else -> Gear.D
    }

    companion object {
        private const val TAG = "SdkVehicle"
    }
}
