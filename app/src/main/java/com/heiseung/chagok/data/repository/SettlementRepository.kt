package com.heiseung.chagok.data.repository

import com.heiseung.chagok.data.local.dao.MemberDao
import com.heiseung.chagok.data.local.dao.MemberDistance
import com.heiseung.chagok.data.local.dao.SettlementDao
import com.heiseung.chagok.data.local.dao.TripDao
import com.heiseung.chagok.data.local.entity.SettlementEntity
import com.heiseung.chagok.data.local.entity.SettlementShareEntity
import com.heiseung.chagok.data.local.entity.SettlementStatus
import java.time.YearMonth
import kotlin.math.roundToInt

data class ShareItem(
    val memberId: Long,
    val name: String,
    val distanceKm: Double,
    val tripCount: Int,
    val ratio: Double,
    val amount: Int
)

// 04 정산 화면
data class MonthlySettlement(
    val yearMonth: String,
    val status: SettlementStatus,
    val totalDistanceKm: Double,
    val totalCost: Int,
    val shares: List<ShareItem>
)

class SettlementRepository(
    private val settlementDao: SettlementDao,
    private val tripDao: TripDao,
    private val memberDao: MemberDao
) {

    // 완료 전엔 그때그때 계산, 완료 후엔 저장된 결과
    suspend fun getMonthly(yearMonth: YearMonth): MonthlySettlement {
        val key = yearMonth.toString()
        val settlement = settlementDao.getByMonth(key)

        if (settlement?.status == SettlementStatus.COMPLETED) {
            val shares = settlementDao.getShares(settlement.id).map {
                ShareItem(
                    memberId = it.memberId,
                    name = memberDao.getById(it.memberId)?.name ?: "",
                    distanceKm = it.distanceKm,
                    tripCount = it.tripCount,
                    ratio = it.ratio,
                    amount = it.amount
                )
            }
            return MonthlySettlement(key, settlement.status, shares.sumOf { it.distanceKm }, settlement.totalCost, shares)
        }

        val (from, to) = TripRepository.monthRange(yearMonth)
        val distances = tripDao.sumDistanceByMember(from, to)
        val totalCost = settlement?.totalCost ?: 0
        return MonthlySettlement(
            yearMonth = key,
            status = SettlementStatus.OPEN,
            totalDistanceKm = distances.sumOf { it.distanceKm },
            totalCost = totalCost,
            shares = split(distances, totalCost)
        )
    }

    // 04 "충전비 고치기"
    suspend fun updateCost(yearMonth: YearMonth, totalCost: Int): ChagokResult<Unit> {
        val key = yearMonth.toString()
        val settlement = settlementDao.getByMonth(key)
        when {
            settlement == null -> settlementDao.insert(SettlementEntity(yearMonth = key, totalCost = totalCost))
            settlement.status == SettlementStatus.COMPLETED -> return ChagokResult.Failure(ChagokError.SETTLEMENT_COMPLETED)
            else -> settlementDao.update(settlement.copy(totalCost = totalCost))
        }
        return ChagokResult.Success(Unit)
    }

    // 04 "정산 완료": 결과를 저장하고 이후엔 안 바뀜
    suspend fun complete(yearMonth: YearMonth): ChagokResult<MonthlySettlement> {
        val key = yearMonth.toString()
        val existing = settlementDao.getByMonth(key)
        if (existing?.status == SettlementStatus.COMPLETED) {
            return ChagokResult.Failure(ChagokError.SETTLEMENT_COMPLETED)
        }
        val settlement = existing
            ?: SettlementEntity(id = settlementDao.insert(SettlementEntity(yearMonth = key)), yearMonth = key)

        val current = getMonthly(yearMonth)
        settlementDao.complete(
            settlement.copy(status = SettlementStatus.COMPLETED, completedAt = System.currentTimeMillis()),
            current.shares.map {
                SettlementShareEntity(
                    settlementId = settlement.id,
                    memberId = it.memberId,
                    distanceKm = it.distanceKm,
                    tripCount = it.tripCount,
                    ratio = it.ratio,
                    amount = it.amount
                )
            }
        )
        return ChagokResult.Success(current.copy(status = SettlementStatus.COMPLETED))
    }

    companion object {
        // 달린 거리 비율로 나누고 원 단위 반올림, 남는 차액은 가장 많이 탄 사람에게
        fun split(distances: List<MemberDistance>, totalCost: Int): List<ShareItem> {
            val total = distances.sumOf { it.distanceKm }
            if (total <= 0.0) return emptyList()

            val shares = distances.map {
                val ratio = it.distanceKm / total
                ShareItem(
                    memberId = it.memberId,
                    name = it.memberName,
                    distanceKm = it.distanceKm,
                    tripCount = it.tripCount,
                    ratio = (ratio * 100).roundToInt() / 100.0,
                    amount = (totalCost * ratio).roundToInt()
                )
            }.sortedByDescending { it.distanceKm }

            val diff = totalCost - shares.sumOf { it.amount }
            return if (diff == 0) shares
            else listOf(shares.first().copy(amount = shares.first().amount + diff)) + shares.drop(1)
        }
    }
}
