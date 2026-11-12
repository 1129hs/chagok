package com.heiseung.chagok.ui.settle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.data.repository.MonthlySettlement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class SettleUiState(
    val loading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val monthly: MonthlySettlement? = null,
    // 충전비 입력칸을 열어둔 상태
    val editingCost: Boolean = false
)

class SettleViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(SettleUiState())
    val state: StateFlow<SettleUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        val month = _state.value.month
        val monthly = container.settlementRepository.getMonthly(month)
        _state.update { it.copy(loading = false, monthly = monthly) }
    }

    fun startEditCost() = _state.update { it.copy(editingCost = true) }

    fun cancelEditCost() = _state.update { it.copy(editingCost = false) }

    // "충전비 고치기" → 저장하면 다시 나눠서 보여줌
    fun saveCost(input: String) = viewModelScope.launch {
        val cost = input.filter { it.isDigit() }.toIntOrNull() ?: return@launch
        container.settlementRepository.updateCost(_state.value.month, cost)
        _state.update { it.copy(editingCost = false) }
        load()
    }

    // "정산 완료"
    fun complete() = viewModelScope.launch {
        container.settlementRepository.complete(_state.value.month)
        load()
    }
}
