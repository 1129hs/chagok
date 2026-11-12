package com.heiseung.chagok.data.repository

import com.heiseung.chagok.data.local.dao.MemberDao
import com.heiseung.chagok.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

// 01 탑승 화면용 운전자 추정 결과
data class IdentifyResult(
    // 기준 안에 들어온 사람, 없으면 null → 화면에서 사람 고르기
    val matched: MemberEntity?,
    val diff: Int?,
    val candidates: List<MemberEntity>
)

class MemberRepository(private val memberDao: MemberDao) {

    fun observeMembers(): Flow<List<MemberEntity>> = memberDao.observeActive()

    suspend fun getMember(id: Long): MemberEntity? = memberDao.getById(id)

    // 06-1 사람 추가하기
    suspend fun addMember(name: String, seatPosition: Int): ChagokResult<MemberEntity> {
        if (seatPosition !in SEAT_RANGE) return ChagokResult.Failure(ChagokError.INVALID_SEAT_POSITION)
        val member = MemberEntity(
            name = name.trim(),
            seatPosition = seatPosition,
            createdAt = System.currentTimeMillis()
        )
        val id = memberDao.insert(member)
        return ChagokResult.Success(member.copy(id = id))
    }

    suspend fun updateMember(id: Long, name: String? = null, seatPosition: Int? = null): ChagokResult<MemberEntity> {
        val current = memberDao.getById(id) ?: return ChagokResult.Failure(ChagokError.MEMBER_NOT_FOUND)
        if (seatPosition != null && seatPosition !in SEAT_RANGE) {
            return ChagokResult.Failure(ChagokError.INVALID_SEAT_POSITION)
        }
        val updated = current.copy(
            name = name?.trim() ?: current.name,
            seatPosition = seatPosition ?: current.seatPosition
        )
        memberDao.update(updated)
        return ChagokResult.Success(updated)
    }

    // 지난 기록이 남도록 실제 삭제 대신 비활성화
    suspend fun deleteMember(id: Long) = memberDao.deactivate(id)

    // 지금 시트 위치와 가장 가까운 사람 찾기
    suspend fun identify(seatPosition: Int): IdentifyResult {
        val members = memberDao.getActive()
        val closest = members.minByOrNull { abs(it.seatPosition - seatPosition) }
        val diff = closest?.let { abs(it.seatPosition - seatPosition) }
        return if (closest != null && diff != null && diff <= MATCH_TOLERANCE) {
            IdentifyResult(matched = closest, diff = diff, candidates = members)
        } else {
            IdentifyResult(matched = null, diff = null, candidates = members)
        }
    }

    companion object {
        val SEAT_RANGE = 0..100
        // 차이가 이 값 이하면 같은 사람으로 봄
        const val MATCH_TOLERANCE = 8
    }
}
