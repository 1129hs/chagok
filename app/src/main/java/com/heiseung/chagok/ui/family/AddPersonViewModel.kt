package com.heiseung.chagok.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.data.repository.ChagokResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddPersonUiState(
    val seatPosition: Int = 0,
    // 지금 위치가 이미 있는 사람이랑 너무 가까우면 알려줌
    val tooCloseTo: MemberEntity? = null
)

class AddPersonViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<AddPersonUiState> = combine(
        container.vehicleDataSource.snapshot.map { it.seatPosition },
        container.memberRepository.observeMembers()
    ) { seat, _ ->
        val result = container.memberRepository.identify(seat)
        AddPersonUiState(seatPosition = seat, tooCloseTo = result.matched)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddPersonUiState())

    // "이 위치로 등록하기"
    fun register(name: String, onDone: () -> Unit) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val result = container.memberRepository.addMember(name, state.value.seatPosition)
        if (result is ChagokResult.Success) onDone()
    }
}
