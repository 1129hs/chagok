package com.heiseung.chagok.data.repository

import android.content.Context
import com.heiseung.chagok.data.local.ChagokDatabase

// Spring의 Bean 주입 대신 쓰는 간단한 보관함, 앱 전체에서 하나만 생성
class AppContainer private constructor(context: Context) {

    private val db = ChagokDatabase.get(context)

    val vehicleRepository = VehicleRepository(db.vehicleDao())
    val memberRepository = MemberRepository(db.memberDao())
    val tripRepository = TripRepository(db.tripDao())
    val handoverRepository = HandoverRepository(db.handoverDao(), db.tripDao())
    val settlementRepository = SettlementRepository(db.settlementDao(), db.tripDao(), db.memberDao())

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun get(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context.applicationContext).also { instance = it }
            }
    }
}
