package com.heiseung.chagok.data.repository

import com.heiseung.chagok.data.local.dao.VehicleDao
import com.heiseung.chagok.data.local.entity.EnergyType
import com.heiseung.chagok.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    fun observe(): Flow<VehicleEntity?> = vehicleDao.observe()

    // 앱 처음 실행 시 차 정보가 없으면 만들어 둠
    suspend fun ensureVehicle(name: String = "우리 차", energyType: EnergyType = EnergyType.EV) {
        if (vehicleDao.get() == null) {
            vehicleDao.upsert(
                VehicleEntity(name = name, energyType = energyType, createdAt = System.currentTimeMillis())
            )
        }
    }

    // 06 설정 화면 토글, 바꿀 것만 넘기기
    suspend fun updateSettings(autoDetectDriver: Boolean? = null, showHandover: Boolean? = null) {
        val current = vehicleDao.get() ?: return
        vehicleDao.upsert(
            current.copy(
                autoDetectDriver = autoDetectDriver ?: current.autoDetectDriver,
                showHandover = showHandover ?: current.showHandover
            )
        )
    }
}
