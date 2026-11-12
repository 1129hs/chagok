package com.heiseung.chagok.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.heiseung.chagok.data.local.entity.HandoverEntity
import com.heiseung.chagok.data.local.entity.HandoverTagEntity
import com.heiseung.chagok.data.local.entity.MemberEntity

@Dao
interface HandoverDao {

    @Insert
    suspend fun insert(handover: HandoverEntity): Long

    @Insert
    suspend fun insertTags(tags: List<HandoverTagEntity>)

    // 인계 + 태그를 한 번에 저장
    @Transaction
    suspend fun insertWithTags(handover: HandoverEntity, tags: List<HandoverTagEntity>): Long {
        val id = insert(handover)
        insertTags(tags.map { it.copy(handoverId = id) })
        return id
    }

    // 가장 최근 인계가 아직 확인 안 된 경우에만 (예전 인계는 안 보여줌)
    @Query(
        """
        SELECT * FROM handover
        WHERE confirmedAt IS NULL
          AND id = (SELECT MAX(id) FROM handover)
        """
    )
    suspend fun getPending(): HandoverEntity?

    @Query("SELECT * FROM handover_tag WHERE handoverId = :handoverId")
    suspend fun getTags(handoverId: Long): List<HandoverTagEntity>

    // 인계를 남긴 사람 = 그 운행의 운전자
    @Query(
        """
        SELECT m.* FROM member m
        JOIN trip t ON t.memberId = m.id
        WHERE t.id = :tripId
        """
    )
    suspend fun getAuthor(tripId: Long): MemberEntity?

    @Query("UPDATE handover SET confirmedBy = :memberId, confirmedAt = :at WHERE id = :id")
    suspend fun confirm(id: Long, memberId: Long, at: Long)
}
