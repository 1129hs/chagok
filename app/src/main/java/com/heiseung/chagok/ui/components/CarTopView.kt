package com.heiseung.chagok.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.heiseung.chagok.ui.theme.CarBody
import com.heiseung.chagok.ui.theme.CarGlass
import com.heiseung.chagok.ui.theme.CarTire
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Line

// 위에서 본 차 (디자인 SVG를 그대로 옮김, 기준 크기 640 x 260)
@Composable
fun CarTopView(modifier: Modifier = Modifier, highlightDriverSeat: Boolean = false) {
    Canvas(modifier = modifier.aspectRatio(640f / 260f)) {
        val s = size.width / 640f
        fun r(x: Float, y: Float, w: Float, h: Float, rad: Float, color: androidx.compose.ui.graphics.Color) =
            drawRoundRect(color, Offset(x * s, y * s), Size(w * s, h * s), CornerRadius(rad * s))

        // 바퀴
        r(130f, 16f, 80f, 22f, 10f, CarTire); r(450f, 16f, 80f, 22f, 10f, CarTire)
        r(130f, 222f, 80f, 22f, 10f, CarTire); r(450f, 222f, 80f, 22f, 10f, CarTire)
        // 차체
        r(30f, 30f, 580f, 200f, 100f, CarBody)
        drawRoundRect(Line, Offset(30f * s, 30f * s), Size(580f * s, 200f * s), CornerRadius(100f * s), style = Stroke(2f * s))
        // 앞유리, 뒷유리
        glass(s, front = true); glass(s, front = false)
        // 실내
        r(220f, 56f, 180f, 148f, 28f, CarGlass)
        if (highlightDriverSeat) {
            // 운전석(앞 왼쪽)만 강조
            r(340f, 66f, 46f, 54f, 12f, Highlight)
            drawRoundRect(Ink, Offset(340f * s, 66f * s), Size(46f * s, 54f * s), CornerRadius(12f * s), style = Stroke(2f * s))
            r(340f, 140f, 46f, 54f, 12f, Line)
            r(240f, 66f, 70f, 128f, 14f, Line)
        }
    }
}

private fun DrawScope.glass(s: Float, front: Boolean) {
    val p = Path()
    if (front) {
        p.moveTo(400f * s, 52f * s); p.quadraticTo(450f * s, 60f * s, 460f * s, 130f * s)
        p.quadraticTo(450f * s, 200f * s, 400f * s, 208f * s)
    } else {
        p.moveTo(180f * s, 60f * s); p.quadraticTo(150f * s, 70f * s, 145f * s, 130f * s)
        p.quadraticTo(150f * s, 190f * s, 180f * s, 200f * s)
    }
    p.close()
    drawPath(p, CarGlass)
}
