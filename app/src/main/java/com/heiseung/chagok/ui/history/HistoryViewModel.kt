package com.heiseung.chagok.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.local.dao.TripListItem
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.repository.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class HistoryUiState(
    val loading: Boolean = true,
    val members: List<MemberEntity> = emptyList(),
    // null이면 전체
    val filter: Long? = null,
    val trips: List<TripListItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val container: AppContainer) : ViewModel() {

    private val filter = MutableStateFlow<Long?>(null)

    // 이번 달 운행, 위의 탭으로 사람별 보기
    val state: StateFlow<HistoryUiState> = combine(
        container.memberRepository.observeMembers(),
        filter,
        filter.flatMapLatest { container.tripRepository.observeMonth(YearMonth.now(), it) }
    ) { members, selected, trips ->
        HistoryUiState(loading = false, members = members, filter = selected, trips = trips)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun select(memberId: Long?) {
        filter.value = memberId
    }
}
