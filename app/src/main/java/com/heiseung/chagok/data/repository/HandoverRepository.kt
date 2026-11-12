package com.heiseung.chagok.data.repository

import com.heiseung.chagok.data.local.dao.HandoverDao
import com.heiseung.chagok.data.local.dao.TripDao
import com.heiseung.chagok.data.local.entity.HandoverEntity
import com.heiseung.chagok.data.local.entity.HandoverTag
import com.heiseung.chagok.data.local.entity.HandoverTagEntity
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.local.entity.TripStatus

// 하차 시점에 Vehicle SDK로 읽은 차 상태
data class VehicleStatus(
    val energyPct: Int,
    val warningCount: Int,
    val doorsClosed: Boolean,
    val windowsClosed: Boolean
)

// 02 인계 화면용 (이전 운전자의 거리·시간은 일부러 안 넣음)
data class PendingHandover(
    val handoverId: Long,
    val from: MemberEntity,
    val createdAt: Long,
    val message: String?,
    val tags: List<HandoverTag>,
    val status: VehicleStatus,
    val energyLow: Boolean
)

class HandoverRepository(
    private val handoverDao: HandoverDao,
    private val tripDao: TripDao
) {

    // 없으면 null → 02 건너뛰고 바로 홈
    suspend fun getPending(): PendingHandover? {
        val handover = handoverDao.getPending() ?: return null
        val author = handoverDao.getAuthor(handover.tripId) ?: return null
        return PendingHandover(
            handoverId = handover.id,
            from = author,
            createdAt = handover.createdAt,
            message = handover.message,
            tags = handoverDao.getTags(handover.id).map { it.tag },
            status = VehicleStatus(
                energyPct = handover.energyPct,
                warningCount = handover.warningCount,
                doorsClosed = handover.doorsClosed,
                windowsClosed = handover.windowsClosed
            ),
            energyLow = handover.energyPct <= LOW_ENERGY_PCT
        )
    }

    // 02 "확인했어요"
    suspend fun confirm(handoverId: Long, memberId: Long): ChagokResult<Unit> {
        val pending = handoverDao.getPending()
        if (pending == null || pending.id != handoverId) {
            return ChagokResult.Failure(ChagokError.HANDOVER_ALREADY_CONFIRMED)
        }
        handoverDao.confirm(handoverId, memberId, System.currentTimeMillis())
        return ChagokResult.Success(Unit)
    }

    // 03 "남기고 내리기"
    suspend fun create(
        tripId: Long,
        tags: List<HandoverTag>,
        message: String?,
        status: VehicleStatus
    ): ChagokResult<Long> {
        val trip = tripDao.getById(tripId) ?: return ChagokResult.Failure(ChagokError.TRIP_NOT_FOUND)
        if (trip.status != TripStatus.COMPLETED) return ChagokResult.Failure(ChagokError.TRIP_NOT_DRIVING)
        val handover = HandoverEntity(
            tripId = tripId,
            message = message?.trim()?.ifBlank { null },
            energyPct = status.energyPct,
            warningCount = status.warningCount,
            doorsClosed = status.doorsClosed,
            windowsClosed = status.windowsClosed,
            createdAt = System.currentTimeMillis()
        )
        return try {
            val id = handoverDao.insertWithTags(
                handover,
                tags.distinct().map { HandoverTagEntity(handoverId = 0, tag = it) }
            )
            ChagokResult.Success(id)
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // tripId unique 위반 = 이미 인계를 남긴 운행
            ChagokResult.Failure(ChagokError.HANDOVER_ALREADY_EXISTS)
        }
    }

    companion object {
        // 이 값 이하면 "충전 필요"로 표시
        const val LOW_ENERGY_PCT = 30
    }
}
