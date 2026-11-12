package com.heiseung.chagok.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

// 차량 화면이라 라이트 고정, 기기 테마색(dynamic color)은 안 씀
private val ChagokColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    outline = Line
)

// 디자인 시안 기준 가로 폭(dp). 앱 영역이 이 폭이 되도록 dp·sp를 같이 늘림
// 값을 줄이면 전체가 더 커짐 (너무 줄이면 화면이 넘칠 수 있음)
const val DESIGN_WIDTH_DP = 1440f

@Composable
fun ChagokTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChagokColors,
        typography = Typography
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val base = LocalDensity.current
            // 앱 영역이 시안보다 넓으면 그만큼 확대, 좁으면 그대로
            val scale = (maxWidth.value / DESIGN_WIDTH_DP).coerceAtLeast(1f)
            CompositionLocalProvider(
                LocalDensity provides Density(base.density * scale, base.fontScale)
            ) {
                content()
            }
        }
    }
}
