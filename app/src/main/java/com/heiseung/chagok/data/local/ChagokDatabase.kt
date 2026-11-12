package com.heiseung.chagok.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heiseung.chagok.data.local.dao.HandoverDao
import com.heiseung.chagok.data.local.dao.MemberDao
import com.heiseung.chagok.data.local.dao.SettlementDao
import com.heiseung.chagok.data.local.dao.TripDao
import com.heiseung.chagok.data.local.dao.VehicleDao
import com.heiseung.chagok.data.local.entity.HandoverEntity
import com.heiseung.chagok.data.local.entity.HandoverTagEntity
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.data.local.entity.SettlementEntity
import com.heiseung.chagok.data.local.entity.SettlementShareEntity
import com.heiseung.chagok.data.local.entity.TripEntity
import com.heiseung.chagok.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        MemberEntity::class,
        TripEntity::class,
        HandoverEntity::class,
        HandoverTagEntity::class,
        SettlementEntity::class,
        SettlementShareEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChagokDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun memberDao(): MemberDao
    abstract fun tripDao(): TripDao
    abstract fun handoverDao(): HandoverDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile
        private var instance: ChagokDatabase? = null

        // 앱 전체에서 DB 하나만 쓰도록
        fun get(context: Context): ChagokDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChagokDatabase::class.java,
                    "chagok.db"
                ).build().also { instance = it }
            }
    }
}
