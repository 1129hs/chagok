package com.heiseung.chagok.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Paper

// 옆에서 본 작은 차 (정산 막대 위를 달리는 차, 기준 크기 72 x 36)
@Composable
fun SmallCar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(2f)) {
        val s = size.width / 72f
        fun o(x: Float, y: Float) = Offset(x * s, y * s)

        // 차체
        val body = Path().apply {
            moveTo(6f * s, 24f * s)
            quadraticTo(6f * s, 16f * s, 14f * s, 15f * s)
            lineTo(22f * s, 7f * s)
            quadraticTo(25f * s, 4f * s, 30f * s, 4f * s)
            lineTo(44f * s, 4f * s)
            quadraticTo(49f * s, 4f * s, 52f * s, 8f * s)
            lineTo(58f * s, 15f * s)
            quadraticTo(66f * s, 16f * s, 66f * s, 24f * s)
            lineTo(66f * s, 27f * s)
            quadraticTo(66f * s, 29f * s, 64f * s, 29f * s)
            lineTo(8f * s, 29f * s)
            quadraticTo(6f * s, 29f * s, 6f * s, 27f * s)
            close()
        }
        drawPath(body, Ink)

        // 창문 두 개
        val back = Path().apply {
            moveTo(25f * s, 9f * s)
            quadraticTo(27f * s, 7f * s, 30f * s, 7f * s)
            lineTo(37f * s, 7f * s); lineTo(37f * s, 15f * s); lineTo(19f * s, 15f * s)
            close()
        }
        val front = Path().apply {
            moveTo(40f * s, 7f * s); lineTo(44f * s, 7f * s)
            quadraticTo(47f * s, 7f * s, 49f * s, 10f * s)
            lineTo(53f * s, 15f * s); lineTo(40f * s, 15f * s)
            close()
        }
        drawPath(back, Paper, alpha = 0.85f)
        drawPath(front, Paper, alpha = 0.85f)

        // 바퀴 (배경색 테두리)
        listOf(19f, 53f).forEach { x ->
            drawCircle(Ink, radius = 6f * s, center = o(x, 29f))
            drawCircle(Paper, radius = 6f * s, center = o(x, 29f), style = Stroke(2f * s))
        }
        // 전조등
        drawRoundRect(Highlight, o(62f, 19f), Size(4f * s, 3f * s), CornerRadius(1f * s))
    }
}
