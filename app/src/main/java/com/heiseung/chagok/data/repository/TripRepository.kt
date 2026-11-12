package com.heiseung.chagok.data.repository

import com.heiseung.chagok.data.local.dao.TripDao
import com.heiseung.chagok.data.local.dao.TripListItem
import com.heiseung.chagok.data.local.entity.TripEntity
import com.heiseung.chagok.data.local.entity.TripStatus
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.roundToInt

// 03 하차 화면에 보여줄 이번 운행 요약
data class TripSummary(
    val tripId: Long,
    val distanceKm: Double,
    val durationMin: Int,
    val energyUsedPct: Int,
    val endEnergyPct: Int
)

class TripRepository(private val tripDao: TripDao) {

    suspend fun getDriving(): TripEntity? = tripDao.getDriving()

    suspend fun getTrip(id: Long): TripEntity? = tripDao.getById(id)

    // P단 해제 시 차량 감시 서비스가 호출
    suspend fun startTrip(
        memberId: Long,
        odometerKm: Double,
        energyPct: Int,
        originName: String? = null,
        startedAt: Long = System.currentTimeMillis()
    ): ChagokResult<TripEntity> {
        if (tripDao.getDriving() != null) return ChagokResult.Failure(ChagokError.TRIP_ALREADY_DRIVING)
        val trip = TripEntity(
            memberId = memberId,
            startedAt = startedAt,
            startOdometerKm = odometerKm,
            startEnergyPct = energyPct,
            originName = originName
        )
        val id = tripDao.insert(trip)
        return ChagokResult.Success(trip.copy(id = id))
    }

    // 다시 P단이면 차량 감시 서비스가 호출 → 결과로 03 화면 그림
    suspend fun endTrip(
        odometerKm: Double,
        energyPct: Int,
        destinationName: String? = null,
        endedAt: Long = System.currentTimeMillis()
    ): ChagokResult<TripSummary> {
        val trip = tripDao.getDriving() ?: return ChagokResult.Failure(ChagokError.TRIP_NOT_DRIVING)
        // 소수 첫째 자리까지, 음수 방지
        val distance = max(0.0, ((odometerKm - trip.startOdometerKm) * 10).roundToInt() / 10.0)
        tripDao.update(
            trip.copy(
                status = TripStatus.COMPLETED,
                endedAt = endedAt,
                endOdometerKm = odometerKm,
                distanceKm = distance,
                endEnergyPct = energyPct,
                destinationName = destinationName
            )
        )
        return ChagokResult.Success(
            TripSummary(
                tripId = trip.id,
                distanceKm = distance,
                durationMin = ((endedAt - trip.startedAt) / 60_000L).toInt(),
                energyUsedPct = max(0, trip.startEnergyPct - energyPct),
                endEnergyPct = energyPct
            )
        )
    }

    // 05 기록 화면, memberId가 null이면 전체
    fun observeMonth(yearMonth: YearMonth, memberId: Long? = null): Flow<List<TripListItem>> {
        val (from, to) = monthRange(yearMonth)
        return tripDao.observeCompleted(memberId, from, to)
    }

    companion object {
        // 그 달 1일 0시 ~ 다음 달 1일 0시 (밀리초)
        fun monthRange(yearMonth: YearMonth): Pair<Long, Long> {
            val zone = ZoneId.systemDefault()
            val from = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val to = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return from to to
        }
    }
}
