package com.heiseung.chagok.ui.handover

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.entity.HandoverTag
import com.heiseung.chagok.data.repository.PendingHandover
import com.heiseung.chagok.ui.components.CarMarks
import com.heiseung.chagok.ui.components.HandoverCarView
import com.heiseung.chagok.ui.components.LinkButton
import com.heiseung.chagok.ui.components.PillButton
import com.heiseung.chagok.ui.components.SpeakOnce
import com.heiseung.chagok.ui.components.highlighted
import com.heiseung.chagok.ui.components.iGa
import com.heiseung.chagok.ui.theme.Body
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Paper
import com.heiseung.chagok.ui.theme.Sub
import com.heiseung.chagok.vehicle.VehicleSnapshot
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 02 인계: 남긴 말 → (2.8초 뒤) 위에서 본 차로 상태 보여주기
@Composable
fun ColumnScope.HandoverScreen(
    state: HandoverUiState,
    current: VehicleSnapshot,
    lowEnergyPct: Int,
    onConfirm: () -> Unit,
    onCharger: () -> Unit
) {
    if (state.loading) return
    val pending = state.pending
    if (pending == null || state.confirmed) {
        // 받을 인계가 없거나 확인 끝: 지금 차 상태만
        val low = current.energyPct <= lowEnergyPct
        Box(Modifier.weight(1f).fillMaxWidth()) {
            CarStatusScene(
                title = if (state.confirmed) "확인했어요.\n안전 운전하세요" else "지금\n차 상태예요",
                sub = null,
                quote = null,
                marks = CarMarks(
                    energyPct = current.energyPct,
                    energyLow = low,
                    doorOpen = !current.doorsClosed,
                    windowOpen = !current.windowsClosed,
                    warningCount = current.warningCount
                ),
                onCharger = onCharger,
                onBack = null
            )
        }
        return
    }

    // 탈 때 인계 메시지를 소리로 읽어줌
    val spoken = buildList {
        add("${pending.from.name.iGa()} 남긴 말이에요.")
        pending.message?.let { add(it) }
        addAll(pending.tags.map { tagLabel(it) })
    }.joinToString(" ")
    SpeakOnce(spoken)

    // 0 = 메시지, 1 = 차 상태
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(pending.handoverId) {
        delay(2800)
        step = 1
    }
    val msgAlpha by animateFloatAsState(if (step == 0) 1f else 0f, tween(500), label = "msgAlpha")
    val msgX by animateDpAsState(if (step == 0) 0.dp else (-160).dp, tween(700), label = "msgX")
    val carX by animateDpAsState(if (step == 1) 0.dp else 640.dp, tween(900), label = "carX")
    val labelAlpha by animateFloatAsState(if (step == 1) 1f else 0f, tween(400, delayMillis = 600), label = "label")

    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        // 차 장면 (오른쪽에서 들어옴)
        Box(Modifier.fillMaxSize().offset(x = carX)) {
            CarStatusScene(
                title = "지금\n차 상태예요",
                sub = "${pending.from.name.iGa()} 넘겨준 차",
                quote = pending.message ?: tagText(pending.tags),
                marks = CarMarks(
                    energyPct = pending.status.energyPct,
                    energyLow = pending.energyLow,
                    luggage = HandoverTag.LUGGAGE in pending.tags,
                    washed = HandoverTag.WASHED in pending.tags,
                    parkedB2 = HandoverTag.PARKING_SPOT in pending.tags,
                    doorOpen = !pending.status.doorsClosed,
                    windowOpen = !pending.status.windowsClosed,
                    warningCount = pending.status.warningCount
                ),
                onCharger = onCharger,
                onBack = { step = 0 },
                labelAlpha = labelAlpha,
                showActions = step == 1
            )
        }
        // 메시지 장면
        if (msgAlpha > 0.01f) {
            Box(Modifier.fillMaxSize().background(Paper.copy(alpha = msgAlpha)).offset(x = msgX).alpha(msgAlpha)) {
                MessageScene(pending, onConfirm = onConfirm, onNext = { step = 1 })
            }
        }
    }
    StepDots(step)
}

@Composable
private fun MessageScene(pending: PendingHandover, onConfirm: () -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight().width(980.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Avatar(pending.from.name)
            Text("${pending.from.name.iGa()} 남긴 말 · ${timeText(pending.createdAt)}", style = ChagokText.Label.copy(fontSize = 22.sp))
        }
        val headline = pending.message ?: tagText(pending.tags) ?: "차를 넘겨줬어요."
        Text(headline, style = ChagokText.Hero.copy(fontSize = 72.sp, lineHeight = 84.sp))
        // 제목으로 안 쓴 한마디는 아래에 한 줄씩
        val extras = pending.tags.map { tagLabel(it) }.filter { it != headline }
        if (extras.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                extras.forEach {
                    Text(it, style = ChagokText.Title.copy(fontSize = 36.sp, lineHeight = 46.sp, fontWeight = FontWeight.SemiBold))
                }
            }
        }
        Text(
            if (pending.energyLow) highlighted("배터리가 ", "${pending.status.energyPct}%", " 남았으니 충전이 필요해요.")
            else highlighted("배터리는 ", "${pending.status.energyPct}%", " 남아 있어요."),
            style = ChagokText.Lead.copy(fontSize = 30.sp, lineHeight = 46.sp)
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            PillButton("확인했어요", onClick = onConfirm)
            LinkButton("차 상태 보기 →", onClick = onNext)
        }
    }
}

// 왼쪽: 큰 제목 + 남긴 말 / 오른쪽: 상태를 그린 차
@Composable
private fun CarStatusScene(
    title: String,
    sub: String?,
    quote: String?,
    marks: CarMarks,
    onCharger: () -> Unit,
    onBack: (() -> Unit)?,
    labelAlpha: Float = 1f,
    showActions: Boolean = true
) {
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.width(470.dp).alpha(labelAlpha),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (sub != null) Text(sub, fontSize = 24.sp, color = Sub)
            Text(title, style = ChagokText.Hero.copy(fontSize = 56.sp, lineHeight = 66.sp))
            if (quote != null) {
                Row(Modifier.padding(top = 12.dp).height(IntrinsicSize.Min)) {
                    Box(Modifier.width(4.dp).fillMaxHeight().background(Highlight))
                    Text("“$quote”", fontSize = 28.sp, lineHeight = 42.sp, color = Body, modifier = Modifier.padding(start = 20.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
            if (showActions) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (marks.energyLow) PillButton("근처 충전소 안내", onClick = onCharger)
                    if (onBack != null) LinkButton("← 메시지 다시 보기", onClick = onBack)
                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            HandoverCarView(marks, Modifier.fillMaxWidth(), labelAlpha = labelAlpha)
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Box(Modifier.size(48.dp).background(Ink, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(1), color = Paper, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepDots(step: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf(0, 1).forEach { i ->
            Box(
                Modifier.padding(horizontal = 5.dp).height(8.dp).width(if (i == step) 28.dp else 8.dp)
                    .background(Ink, CircleShape)
            )
        }
    }
}

private fun timeText(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("a h:mm"))

private fun tagLabel(tag: HandoverTag): String = when (tag) {
    HandoverTag.CHARGE_NEEDED -> "충전이 필요해요."
    HandoverTag.LUGGAGE -> "차에 짐이 있어요."
    HandoverTag.WASHED -> "세차해뒀어요."
    HandoverTag.PARKING_SPOT -> "B2에 세웠어요."
}

private fun tagText(tags: List<HandoverTag>): String? = when {
    HandoverTag.CHARGE_NEEDED in tags -> "충전이 필요해요."
    HandoverTag.LUGGAGE in tags -> "차에 짐이 있어요."
    HandoverTag.WASHED in tags -> "세차해뒀어요."
    HandoverTag.PARKING_SPOT in tags -> "B2에 세웠어요."
    else -> null
}