package com.heiseung.chagok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "handover_tag",
    foreignKeys = [
        ForeignKey(
            entity = HandoverEntity::class,
            parentColumns = ["id"],
            childColumns = ["handoverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("handoverId")]
)
data class HandoverTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val handoverId: Long,
    val tag: HandoverTag
)
