package com.heiseung.chagok.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.local.entity.VehicleEntity
import com.heiseung.chagok.data.repository.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FamilyUiState(
    val loading: Boolean = true,
    val members: List<MemberEntity> = emptyList(),
    val vehicle: VehicleEntity? = null
)

class FamilyViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<FamilyUiState> = combine(
        container.memberRepository.observeMembers(),
        container.vehicleRepository.observe()
    ) { members, vehicle ->
        FamilyUiState(loading = false, members = members, vehicle = vehicle)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyUiState())

    fun setAutoDetect(on: Boolean) = viewModelScope.launch {
        container.vehicleRepository.updateSettings(autoDetectDriver = on)
    }

    fun setShowHandover(on: Boolean) = viewModelScope.launch {
        container.vehicleRepository.updateSettings(showHandover = on)
    }
}
