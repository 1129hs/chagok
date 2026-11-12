package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 운행 하나에 인계 최대 1개 (tripId unique)
@Entity(
    tableName = "handover",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"], unique = true)]
)
data class HandoverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val message: String? = null,
    // 하차 시점 차 상태
    val energyPct: Int,
    val warningCount: Int,
    val doorsClosed: Boolean,
    val windowsClosed: Boolean,
    // 확인한 사람, null이면 아직 확인 안 함
    val confirmedBy: Long? = null,
    val confirmedAt: Long? = null,
    val createdAt: Long
)
