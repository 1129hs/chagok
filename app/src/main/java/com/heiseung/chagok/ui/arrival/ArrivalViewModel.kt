package com.heiseung.chagok.ui.arrival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.local.entity.HandoverTag
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.data.repository.VehicleStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

data class ArrivalUiState(
    val loading: Boolean = true,
    val driverName: String = "",
    val distanceKm: Double = 0.0,
    val durationMin: Int = 0,
    val energyUsedPct: Int = 0,
    val endEnergyPct: Int = 0,
    val tags: Set<HandoverTag> = emptySet(),
    val message: String = "",
    // 남기기 끝 → 안내 문구 보여주는 중
    val left: Boolean = false
)

class ArrivalViewModel(private val container: AppContainer, private val tripId: Long) : ViewModel() {

    private val _state = MutableStateFlow(ArrivalUiState())
    val state: StateFlow<ArrivalUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val trip = container.tripRepository.getTrip(tripId) ?: return@launch
            val member = container.memberRepository.getMember(trip.memberId)
            _state.value = ArrivalUiState(
                loading = false,
                driverName = member?.name ?: "",
                distanceKm = trip.distanceKm ?: 0.0,
                durationMin = (((trip.endedAt ?: trip.startedAt) - trip.startedAt) / 60_000L).toInt(),
                energyUsedPct = max(0, trip.startEnergyPct - (trip.endEnergyPct ?: trip.startEnergyPct)),
                endEnergyPct = trip.endEnergyPct ?: trip.startEnergyPct,
                // 배터리가 낮으면 "충전 필요해요"를 미리 골라둠
                tags = if ((trip.endEnergyPct ?: 100) <= 30) setOf(HandoverTag.CHARGE_NEEDED) else emptySet()
            )
        }
    }

    fun toggleTag(tag: HandoverTag) = _state.update {
        it.copy(tags = if (tag in it.tags) it.tags - tag else it.tags + tag)
    }

    fun setMessage(text: String) = _state.update { it.copy(message = text) }

    // "남기고 내리기"
    fun leave(onDone: () -> Unit) = viewModelScope.launch {
        val s = _state.value
        val now = container.vehicleDataSource.snapshot.value
        container.handoverRepository.create(
            tripId = tripId,
            tags = s.tags.toList(),
            message = s.message,
            status = VehicleStatus(now.energyPct, now.warningCount, now.doorsClosed, now.windowsClosed)
        )
        _state.update { it.copy(left = true) }
        delay(1800)
        container.session.clear()
        onDone()
    }

    // "그냥 내릴게요"
    fun skip(onDone: () -> Unit) {
        container.session.clear()
        onDone()
    }
}
