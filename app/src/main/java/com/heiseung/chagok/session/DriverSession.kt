package com.heiseung.chagok.session

import com.heiseung.chagok.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 지금 운전석에 앉은 사람 (탑승 화면에서 정하고, 내리면 비움)
class DriverSession {
    private val _driver = MutableStateFlow<MemberEntity?>(null)
    val driver: StateFlow<MemberEntity?> = _driver.asStateFlow()

    fun set(member: MemberEntity) { _driver.value = member }
    fun clear() { _driver.value = null }
}
