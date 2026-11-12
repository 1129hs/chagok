package com.heiseung.chagok.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.heiseung.chagok.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicle WHERE id = 1")
    fun observe(): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicle WHERE id = 1")
    suspend fun get(): VehicleEntity?

    @Upsert
    suspend fun upsert(vehicle: VehicleEntity)
}
