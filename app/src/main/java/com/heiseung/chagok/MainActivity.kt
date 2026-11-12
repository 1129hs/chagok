package com.heiseung.chagok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.ui.navigation.ChagokNavHost
import com.heiseung.chagok.ui.theme.ChagokTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer.get(this)

        // 처음 실행이면 차 정보 만들고, 기어 감시 시작
        lifecycleScope.launch { container.vehicleRepository.ensureVehicle() }
        container.tripWatcher.start()

        setContent {
            ChagokTheme {
                ChagokNavHost(container)
            }
        }
    }
}
