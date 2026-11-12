package com.heiseung.chagok.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heiseung.chagok.data.local.entity.SettlementEntity
import com.heiseung.chagok.data.local.entity.SettlementShareEntity

@Dao
interface SettlementDao {

    @Query("SELECT * FROM settlement WHERE yearMonth = :yearMonth")
    suspend fun getByMonth(yearMonth: String): SettlementEntity?

    @Insert
    suspend fun insert(settlement: SettlementEntity): Long

    @Update
    suspend fun update(settlement: SettlementEntity)

    @Query("SELECT * FROM settlement_share WHERE settlementId = :settlementId")
    suspend fun getShares(settlementId: Long): List<SettlementShareEntity>

    @Insert
    suspend fun insertShares(shares: List<SettlementShareEntity>)

    // 정산 완료: 상태 변경 + 결과 저장을 한 번에
    @Transaction
    suspend fun complete(settlement: SettlementEntity, shares: List<SettlementShareEntity>) {
        update(settlement)
        insertShares(shares)
    }
}
