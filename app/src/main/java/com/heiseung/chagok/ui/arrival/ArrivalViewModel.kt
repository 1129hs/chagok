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

// 음성 인계 진행 상태
enum class VoiceState { IDLE, RECORDING, THINKING, FAILED, NO_SOUND }

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
    val left: Boolean = false,
    val voice: VoiceState = VoiceState.IDLE,
    // AI가 받아쓴 원래 말 (확인용)
    val heard: String? = null
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

    // "말로 남기기" 누름 → 녹음 시작
    // 녹음 중 가장 큰 소리 (무음 녹음이면 AI를 부르지 않음)
    private var peak = 0

    fun startVoice() {
        val started = runCatching { container.voiceRecorder.start() }
        _state.update { it.copy(voice = if (started.isSuccess) VoiceState.RECORDING else VoiceState.FAILED) }
        if (started.isFailure) return
        peak = 0
        viewModelScope.launch {
            while (_state.value.voice == VoiceState.RECORDING) {
                peak = maxOf(peak, container.voiceRecorder.peakAmplitude())
                delay(200)
            }
        }
    }

    // "다 말했어요" → 녹음 끝, AI가 정리한 말과 한마디를 채워줌 (고칠 수 있음)
    fun finishVoice() = viewModelScope.launch {
        peak = maxOf(peak, container.voiceRecorder.peakAmplitude())
        val file = container.voiceRecorder.stop()
        if (file != null && peak < MIN_PEAK) {
            android.util.Log.w("HandoverAi", "녹음이 무음 (peak=$peak)")
            _state.update { it.copy(voice = VoiceState.NO_SOUND) }
            return@launch
        }
        if (file == null) {
            _state.update { it.copy(voice = VoiceState.FAILED) }
            return@launch
        }
        _state.update { it.copy(voice = VoiceState.THINKING) }
        val result = container.handoverAi.summarize(file).getOrNull()
        if (result == null || result.transcript.isBlank()) {
            _state.update { it.copy(voice = VoiceState.FAILED) }
            return@launch
        }
        _state.update {
            it.copy(
                voice = VoiceState.IDLE,
                message = result.message.ifBlank { result.transcript },
                tags = it.tags + result.tags,
                heard = result.transcript
            )
        }
    }

    companion object {
        // 이보다 작으면 사실상 무음
        private const val MIN_PEAK = 300
    }

    override fun onCleared() {
        container.voiceRecorder.cancel()
    }

    // "그냥 내릴게요"
    fun skip(onDone: () -> Unit) {
        container.session.clear()
        onDone()
    }
}
