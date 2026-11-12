package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlement",
    indices = [Index(value = ["yearMonth"], unique = true)]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // "2026-11" 형식
    val yearMonth: String,
    // 충전비 직접 입력
    val totalCost: Int = 0,
    val status: SettlementStatus = SettlementStatus.OPEN,
    val completedAt: Long? = null
)
