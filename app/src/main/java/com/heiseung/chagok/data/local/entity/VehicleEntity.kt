package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1차는 차 한 대라 id = 1 고정
@Entity(tableName = "vehicle")
data class VehicleEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val energyType: EnergyType,
    val autoDetectDriver: Boolean = true,
    val showHandover: Boolean = true,
    val settlementBasis: SettlementBasis = SettlementBasis.DISTANCE,
    val createdAt: Long
)
