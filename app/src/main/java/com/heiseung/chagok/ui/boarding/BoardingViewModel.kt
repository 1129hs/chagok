package com.heiseung.chagok.ui.boarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.data.repository.ChagokResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class BoardingUiState(
    val loading: Boolean = true,
    val seatPosition: Int = 0,
    // 시트 위치로 찾은 사람, 없으면 고르기
    val matched: MemberEntity? = null,
    val members: List<MemberEntity> = emptyList()
)

class BoardingViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(BoardingUiState())
    val state: StateFlow<BoardingUiState> = _state.asStateFlow()

    init {
        // 시트 위치나 등록된 사람이 바뀔 때마다 다시 알아봄
        viewModelScope.launch {
            val seatFlow = container.vehicleDataSource.snapshot.map { it.seatPosition }.distinctUntilChanged()
            combine(
                seatFlow,
                container.memberRepository.observeMembers(),
                container.vehicleRepository.observe()
            ) { seat, _, vehicle -> seat to (vehicle?.autoDetectDriver ?: true) }
                .collect { (seat, autoDetect) ->
                    val result = container.memberRepository.identify(seat)
                    _state.value = BoardingUiState(
                        loading = false,
                        seatPosition = seat,
                        // 설정에서 끄면 항상 사람 고르기
                        matched = if (autoDetect) result.matched else null,
                        members = result.candidates
                    )
                }
        }
    }

    fun confirm(member: MemberEntity) = container.session.set(member)

    // 새 사람: 지금 시트 위치로 등록하고 바로 운전자로
    fun register(name: String, onDone: (MemberEntity) -> Unit) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val result = container.memberRepository.addMember(name, _state.value.seatPosition)
        if (result is ChagokResult.Success) {
            container.session.set(result.data)
            onDone(result.data)
        }
    }
}
