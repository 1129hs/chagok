package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 정산 완료 시점에 저장하는 사람별 결과
@Entity(
    tableName = "settlement_share",
    foreignKeys = [
        ForeignKey(
            entity = SettlementEntity::class,
            parentColumns = ["id"],
            childColumns = ["settlementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"]
        )
    ],
    indices = [Index("settlementId"), Index("memberId")]
)
data class SettlementShareEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val settlementId: Long,
    val memberId: Long,
    val distanceKm: Double,
    val tripCount: Int,
    val ratio: Double,
    val amount: Int
)
