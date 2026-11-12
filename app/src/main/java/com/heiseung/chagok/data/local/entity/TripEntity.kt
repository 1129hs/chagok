package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"]
        )
    ],
    indices = [Index("memberId"), Index("startedAt")]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val status: TripStatus = TripStatus.DRIVING,
    val startedAt: Long,
    val endedAt: Long? = null,
    val startOdometerKm: Double,
    val endOdometerKm: Double? = null,
    val distanceKm: Double? = null,
    val startEnergyPct: Int,
    val endEnergyPct: Int? = null,
    val originName: String? = null,
    val destinationName: String? = null
)
