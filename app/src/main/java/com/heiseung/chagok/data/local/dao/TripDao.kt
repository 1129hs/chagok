package com.heiseung.chagok.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heiseung.chagok.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

// 05 기록 목록 한 줄
data class TripListItem(
    val tripId: Long,
    val memberId: Long,
    val memberName: String,
    val originName: String?,
    val destinationName: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val distanceKm: Double?,
    val hasHandover: Boolean
)

// 04 정산용 사람별 합계
data class MemberDistance(
    val memberId: Long,
    val memberName: String,
    val distanceKm: Double,
    val tripCount: Int
)

@Dao
interface TripDao {

    @Insert
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)

    @Query("SELECT * FROM trip WHERE id = :id")
    suspend fun getById(id: Long): TripEntity?

    // 진행 중인 운행 (항상 0개 또는 1개)
    @Query("SELECT * FROM trip WHERE status = 'DRIVING' LIMIT 1")
    suspend fun getDriving(): TripEntity?

    // memberId가 null이면 전체
    @Query(
        """
        SELECT t.id AS tripId, t.memberId, m.name AS memberName,
               t.originName, t.destinationName, t.startedAt, t.endedAt, t.distanceKm,
               EXISTS(SELECT 1 FROM handover h WHERE h.tripId = t.id) AS hasHandover
        FROM trip t
        JOIN member m ON m.id = t.memberId
        WHERE t.status = 'COMPLETED'
          AND (:memberId IS NULL OR t.memberId = :memberId)
          AND t.startedAt >= :from AND t.startedAt < :to
        ORDER BY t.startedAt DESC
        """
    )
    fun observeCompleted(memberId: Long?, from: Long, to: Long): Flow<List<TripListItem>>

    @Query(
        """
        SELECT t.memberId, m.name AS memberName,
               SUM(t.distanceKm) AS distanceKm, COUNT(*) AS tripCount
        FROM trip t
        JOIN member m ON m.id = t.memberId
        WHERE t.status = 'COMPLETED'
          AND t.startedAt >= :from AND t.startedAt < :to
        GROUP BY t.memberId
        ORDER BY distanceKm DESC
        """
    )
    suspend fun sumDistanceByMember(from: Long, to: Long): List<MemberDistance>
}
