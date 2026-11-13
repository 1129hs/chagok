package com.heiseung.chagok.data.repository

import android.content.Context
import com.heiseung.chagok.BuildConfig
import com.heiseung.chagok.ai.HandoverAi
import com.heiseung.chagok.ai.VoiceRecorder
import com.heiseung.chagok.data.local.ChagokDatabase
import com.heiseung.chagok.session.DriverSession
import com.heiseung.chagok.vehicle.FakeVehicleDataSource
import com.heiseung.chagok.vehicle.SdkVehicleDataSource
import com.heiseung.chagok.vehicle.TripWatcher
import com.heiseung.chagok.vehicle.VehicleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Spring의 Bean 주입 대신 쓰는 간단한 보관함, 앱 전체에서 하나만 생성
class AppContainer private constructor(context: Context) {

    private val db = ChagokDatabase.get(context)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val vehicleRepository = VehicleRepository(db.vehicleDao())
    val memberRepository = MemberRepository(db.memberDao())
    val tripRepository = TripRepository(db.tripDao())
    val handoverRepository = HandoverRepository(db.handoverDao(), db.tripDao())
    val settlementRepository = SettlementRepository(db.settlementDao(), db.tripDao(), db.memberDao())

    // 실제 차량 값(SDK) 사용, 화면 흐름만 볼 땐 false로 바꾸면 가짜 차 + [출발][도착] 버튼
    val vehicleDataSource: VehicleDataSource =
        if (USE_VEHICLE_SDK) SdkVehicleDataSource(context.applicationContext).also { it.start() }
        else FakeVehicleDataSource()
    val session = DriverSession()
    val tripWatcher = TripWatcher(appScope, vehicleDataSource, tripRepository, session)

    // 음성 인계
    val voiceRecorder = VoiceRecorder(context.applicationContext)
    val handoverAi = HandoverAi(BuildConfig.GEMINI_API_KEY)

    companion object {
        const val USE_VEHICLE_SDK = true

        @Volatile
        private var instance: AppContainer? = null

        fun get(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context.applicationContext).also { instance = it }
            }
    }
}
