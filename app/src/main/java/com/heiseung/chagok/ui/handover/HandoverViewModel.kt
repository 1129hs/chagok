package com.heiseung.chagok.ui.handover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.data.repository.HandoverRepository
import com.heiseung.chagok.data.repository.PendingHandover
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HandoverUiState(
    val loading: Boolean = true,
    // null이면 받을 인계 없음 → 지금 차 상태만
    val pending: PendingHandover? = null,
    val confirmed: Boolean = false
)

class HandoverViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(HandoverUiState())
    val state: StateFlow<HandoverUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val pending = container.handoverRepository.getPending()
            // 인계를 남긴 사람이 다시 타면 굳이 보여주지 않음
            val me = container.session.driver.value
            val visible = pending?.takeIf { me == null || it.from.id != me.id }
            _state.value = HandoverUiState(loading = false, pending = visible)
        }
    }

    // "확인했어요": 인계만 확인 처리 (운행 기록은 기어로 자동 시작)
    fun confirm() = viewModelScope.launch {
        val pending = _state.value.pending ?: return@launch
        val me = container.session.driver.value ?: return@launch
        container.handoverRepository.confirm(pending.handoverId, me.id)
        _state.update { it.copy(confirmed = true) }
    }

    val lowEnergyPct = HandoverRepository.LOW_ENERGY_PCT
}
