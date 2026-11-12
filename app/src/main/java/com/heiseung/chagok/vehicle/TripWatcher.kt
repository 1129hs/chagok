package com.heiseung.chagok.vehicle

import com.heiseung.chagok.data.repository.ChagokResult
import com.heiseung.chagok.data.repository.TripRepository
import com.heiseung.chagok.session.DriverSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class TripEvent {
    data class Started(val tripId: Long) : TripEvent()
    data class Ended(val tripId: Long) : TripEvent()
}

// 기어를 보고 운행을 자동으로 시작·종료 ("확인했어요" 버튼과는 별개)
class TripWatcher(
    private val scope: CoroutineScope,
    private val source: VehicleDataSource,
    private val tripRepository: TripRepository,
    private val session: DriverSession
) {
    private val _events = MutableSharedFlow<TripEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<TripEvent> = _events.asSharedFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            var parked: Boolean? = null
            source.snapshot.map { it.gear == Gear.P }.distinctUntilChanged().collect { isParked ->
                // 앱 켜진 직후 첫 값은 기준으로만 사용
                val before = parked
                parked = isParked
                if (before == null) return@collect

                if (!isParked) onLeaveParking() else onPark()
            }
        }
    }

    private suspend fun onLeaveParking() {
        val driver = session.driver.value ?: return
        val now = source.snapshot.value
        val result = tripRepository.startTrip(driver.id, now.odometerKm, now.energyPct)
        if (result is ChagokResult.Success) _events.emit(TripEvent.Started(result.data.id))
    }

    private suspend fun onPark() {
        val now = source.snapshot.value
        val result = tripRepository.endTrip(now.odometerKm, now.energyPct)
        if (result is ChagokResult.Success) _events.emit(TripEvent.Ended(result.data.tripId))
    }
}
