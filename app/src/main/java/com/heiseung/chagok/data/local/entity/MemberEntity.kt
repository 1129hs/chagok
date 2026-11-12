package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    // Seat Fore Aft Position 값 (0~100)
    val seatPosition: Int,
    // 삭제해도 지난 운행 기록이 남도록 실제로 지우지 않고 false로
    val isActive: Boolean = true,
    val createdAt: Long
)
