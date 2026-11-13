package com.heiseung.chagok.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.ui.theme.CarBody
import com.heiseung.chagok.ui.theme.CarGlass
import com.heiseung.chagok.ui.theme.CarTire
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Line
import com.heiseung.chagok.ui.theme.Warn

// 차 그림 위에 표시할 상태
data class CarMarks(
    val energyPct: Int,
    val energyLow: Boolean,
    val luggage: Boolean = false,
    val washed: Boolean = false,
    val parkedB2: Boolean = false,
    val doorOpen: Boolean = false,
    val windowOpen: Boolean = false,
    val warningCount: Int = 0
)

private val Orange = Color(0xFFC97A00)
private val Sky = Color(0xFF3D9BE9)
private val ParkingLine = Color(0xFFC9CBC6)
private val FloorText = Color(0xFFA9ABA6)

// 디자인 캔버스 좌표(x 540~1420, y 80~600)를 그대로 옮김
private const val X0 = 540f
private const val Y0 = 80f
private const val W = 880f
private const val H = 520f

// 인계 화면의 위에서 본 차: 그림으로 상태를 보여주고, 문제가 있을 때만 주황 글자
@Composable
fun HandoverCarView(marks: CarMarks, modifier: Modifier = Modifier, labelAlpha: Float = 1f) {
    BoxWithConstraints(modifier.aspectRatio(W / H)) {
        val unit = maxWidth / W
        fun dx(x: Float): Dp = unit * (x - X0)
        fun dy(y: Float): Dp = unit * (y - Y0)

        Canvas(Modifier.fillMaxSize()) {
            val s = size.width / W
            fun o(x: Float, y: Float) = Offset((x - X0) * s, (y - Y0) * s)
            fun rr(x: Float, y: Float, w: Float, h: Float, r: Float, c: Color) =
                drawRoundRect(c, o(x, y), Size(w * s, h * s), CornerRadius(r * s))

            // 주차 칸 (B2에 세웠어요)
            if (marks.parkedB2) {
                listOf(
                    o(590f, 226f) to o(1270f, 226f),
                    o(590f, 514f) to o(1270f, 514f),
                    o(590f, 226f) to o(590f, 514f)
                ).forEach { (a, b) -> drawLine(ParkingLine, a, b, 4f * s, StrokeCap.Round) }
            }

            // 바퀴, 차체, 유리
            listOf(740f to 256f, 1060f to 256f, 740f to 462f, 1060f to 462f)
                .forEach { (x, y) -> rr(x, y, 80f, 22f, 10f, CarTire) }
            rr(640f, 270f, 580f, 200f, 100f, CarBody)
            drawRoundRect(Line, o(640f, 270f), Size(580f * s, 200f * s), CornerRadius(100f * s), style = Stroke(2f * s))
            drawPath(glass(s, o(1010f, 292f), o(1060f, 300f), o(1070f, 370f), o(1060f, 440f), o(1010f, 448f)), CarGlass)
            rr(830f, 300f, 170f, 140f, 28f, CarGlass)
            drawPath(glass(s, o(790f, 300f), o(760f, 310f), o(755f, 370f), o(760f, 430f), o(790f, 440f)), CarGlass)

            // 배터리 (실내 바닥에 잔량)
            rr(868f, 355f, 90f, 30f, 7f, Color.White)
            drawRoundRect(Ink, o(868f, 355f), Size(90f * s, 30f * s), CornerRadius(7f * s), style = Stroke(2.5f * s))
            rr(958f, 364f, 6f, 12f, 2f, Ink)
            val fill = 84f * marks.energyPct.coerceIn(0, 100) / 100f
            if (fill > 0f) rr(871f, 358f, fill, 24f, 5f, if (marks.energyLow) Orange else Ink)

            if (marks.luggage) drawParcel(s, ::o)
            if (marks.doorOpen) {
                // 열린 문: 차 옆으로 벌어진 모양
                drawLine(Orange, o(862f, 272f), o(778f, 214f), 10f * s, StrokeCap.Round)
                rr(772f, 268f, 92f, 6f, 0f, Orange)
            }
            if (marks.warningCount > 0) drawWarning(s, ::o)
            if (marks.washed) {
                listOf(
                    Triple(612f, 250f, 14f), Triple(1250f, 250f, 10f), Triple(1262f, 492f, 14f),
                    Triple(630f, 500f, 9f), Triple(930f, 240f, 8f)
                ).forEach { (x, y, r) -> drawPath(sparkle(o(x, y), r * s), Sky) }
            }

            // 문제 있는 것만 설명선
            if (labelAlpha > 0f) {
                val c = Orange.copy(alpha = labelAlpha)
                fun leader(a: Offset, b: Offset) {
                    drawCircle(c, 7f * s, a)
                    drawLine(c, a, b, 2f * s)
                }
                if (marks.energyLow) leader(o(913f, 355f), o(913f, 160f))
                if (marks.doorOpen) leader(o(778f, 214f), o(720f, 160f))
                if (marks.windowOpen) leader(o(913f, 470f), o(913f, 520f))
                if (marks.warningCount > 0) leader(o(1128f, 388f), o(1262f, 388f))
            }
        }

        // 바닥에 칠한 층 표시
        if (marks.parkedB2) {
            Text(
                "B2", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = FloorText,
                modifier = Modifier.offset(dx(586f), dy(292f)).rotate(-90f)
            )
        }

        Box(Modifier.fillMaxSize().alpha(labelAlpha)) {
            if (marks.energyLow) IssueLabel("배터리 ${marks.energyPct}%", "충전 필요", dx(913f) - 140.dp, dy(92f))
            if (marks.doorOpen) IssueLabel("문이 열려 있어요", "내리기 전에 확인", dx(700f) - 140.dp, dy(92f))
            if (marks.windowOpen) IssueLabel("창문이 열려 있어요", null, dx(913f) - 140.dp, dy(526f))
            if (marks.warningCount > 0) IssueLabel("경고등 ${marks.warningCount}개", null, dx(1270f), dy(370f), center = false)
        }
    }
}

@Composable
private fun IssueLabel(title: String, sub: String?, x: Dp, y: Dp, center: Boolean = true) {
    val align = if (center) TextAlign.Center else TextAlign.Start
    Column(Modifier.offset(x, y).width(280.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Warn, textAlign = align, modifier = Modifier.width(280.dp))
        if (sub != null) Text(sub, fontSize = 20.sp, color = Warn, textAlign = align, modifier = Modifier.width(280.dp))
    }
}

// 앞유리·뒷유리 (곡선 두 번)
@Suppress("UNUSED_PARAMETER")
private fun glass(s: Float, a: Offset, c1: Offset, m: Offset, c2: Offset, b: Offset) = Path().apply {
    moveTo(a.x, a.y)
    quadraticTo(c1.x, c1.y, m.x, m.y)
    quadraticTo(c2.x, c2.y, b.x, b.y)
    close()
}

// 세차 반짝이 (네 갈래 별)
private fun sparkle(c: Offset, r: Float) = Path().apply {
    val k = r * 0.18f
    moveTo(c.x, c.y - r)
    quadraticTo(c.x + k, c.y - k, c.x + r, c.y)
    quadraticTo(c.x + k, c.y + k, c.x, c.y + r)
    quadraticTo(c.x - k, c.y + k, c.x - r, c.y)
    quadraticTo(c.x - k, c.y - k, c.x, c.y - r)
    close()
}

// 트렁크 택배 상자
private fun DrawScope.drawParcel(s: Float, o: (Float, Float) -> Offset) {
    fun box(x: Float, y: Float, w: Float, h: Float, fill: Color) {
        drawRoundRect(fill, o(x, y), Size(w * s, h * s), CornerRadius(6f * s))
        drawRoundRect(Ink, o(x, y), Size(w * s, h * s), CornerRadius(6f * s), style = Stroke(2.5f * s))
    }
    box(668f, 332f, 62f, 54f, Highlight)
    drawLine(Ink, o(699f, 332f), o(699f, 386f), 2.5f * s)
    drawLine(Ink, o(668f, 352f), o(730f, 352f), 2f * s)
    box(676f, 392f, 46f, 36f, Color.White)
    drawLine(Ink, o(699f, 392f), o(699f, 428f), 2f * s)
}

// 계기판 경고등
private fun DrawScope.drawWarning(s: Float, o: (Float, Float) -> Offset) {
    val a = o(1128f, 354f); val b = o(1145f, 384f); val c = o(1111f, 384f)
    val tri = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close() }
    drawPath(tri, Highlight)
    drawPath(tri, Ink, style = Stroke(2.5f * s, join = StrokeJoin.Round))
    drawRoundRect(Ink, o(1126.5f, 364f), Size(3f * s, 11f * s), CornerRadius(1.5f * s))
    drawCircle(Ink, 1.8f * s, o(1128f, 379f))
}