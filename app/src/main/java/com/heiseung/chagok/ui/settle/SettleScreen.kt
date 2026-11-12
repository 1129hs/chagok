package com.heiseung.chagok.ui.settle

import com.heiseung.chagok.ui.components.iGa
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.entity.SettlementStatus
import com.heiseung.chagok.data.repository.MonthlySettlement
import com.heiseung.chagok.ui.components.LinkButton
import com.heiseung.chagok.ui.components.PillButton
import com.heiseung.chagok.ui.components.SmallCar
import com.heiseung.chagok.ui.components.km
import com.heiseung.chagok.ui.components.won
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Sub
import kotlin.math.roundToInt

private val Track = Color(0xFFDCDDD8)
private val RideEasing = CubicBezierEasing(0.25f, 0.8f, 0.25f, 1f)

// 04 월간 정산: 달린 거리 막대 위를 차가 달려감
@Composable
fun ColumnScope.SettleScreen(state: SettleUiState, vm: SettleViewModel) {
    val monthly = state.monthly
    if (state.loading || monthly == null) return

    Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("${state.month.monthValue}월 정산", style = ChagokText.Label.copy(fontSize = 20.sp))
            Text(headline(monthly), style = ChagokText.Hero.copy(lineHeight = 78.sp))
        }

        if (monthly.shares.isNotEmpty()) RideBar(monthly)

        if (state.editingCost) {
            CostEditor(monthly.totalCost, onSave = vm::saveCost, onCancel = vm::cancelEditCost)
        } else {
            Text(costText(monthly), style = ChagokText.Lead.copy(fontSize = 28.sp, lineHeight = 46.sp))
            Actions(monthly, vm)
        }
    }
}

private fun headline(m: MonthlySettlement): String {
    if (m.shares.isEmpty()) return "이번 달엔 아직\n운행 기록이 없어요."
    val parts = m.shares.map { "${it.name.iGa()} ${km(it.distanceKm)}" }
    return "이번 달엔 " + parts.joinToString(",\n") + " 탔어요."
}

private fun costText(m: MonthlySettlement) = buildAnnotatedString {
    if (m.shares.isEmpty()) {
        append("운행이 쌓이면 달린 거리대로 나눠드릴게요.")
        return@buildAnnotatedString
    }
    if (m.totalCost <= 0) {
        append("이번 달 충전비를 적으면\n달린 거리대로 나눠드릴게요.")
        return@buildAnnotatedString
    }
    append("충전비 ${won(m.totalCost)}은 달린 거리대로 나눠서\n")
    m.shares.forEachIndexed { i, share ->
        if (i > 0) append(", ")
        append("${share.name} ")
        // 가장 많이 탄 사람 금액만 형광펜
        val style = if (i == 0) SpanStyle(color = Ink, fontWeight = FontWeight.Bold, background = Highlight)
        else SpanStyle(color = Ink, fontWeight = FontWeight.Bold)
        withStyle(style) { append(won(share.amount)) }
    }
    append("이에요.")
}

@Composable
private fun RideBar(m: MonthlySettlement) {
    val top = m.shares.first()
    val target = top.distanceKm / m.totalDistanceKm.coerceAtLeast(0.1)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.snapTo(0f)
        progress.animateTo(target.toFloat(), tween(durationMillis = 1800, delayMillis = 300, easing = RideEasing))
    }

    Column(Modifier.widthIn(max = 1100.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 50.dp)) {
            val carWidth = 88.dp
            val fill = maxWidth * progress.value
            // 회색 길
            Box(Modifier.fillMaxWidth().height(16.dp).background(Track, RoundedCornerShape(8.dp)))
            // 검정으로 채워지는 부분
            Box(Modifier.width(fill).height(16.dp).background(Ink, RoundedCornerShape(8.dp)))
            // 채워진 끝에 차가 서 있음
            SmallCar(Modifier.width(carWidth).offset(x = fill - carWidth, y = (-42).dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            m.shares.forEachIndexed { i, share ->
                Text(
                    "${share.name} ${(share.ratio * 100).roundToInt()}%",
                    fontSize = 19.sp,
                    fontWeight = if (i == 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (i == 0) Ink else Sub
                )
            }
        }
    }
}

@Composable
private fun Actions(m: MonthlySettlement, vm: SettleViewModel) {
    if (m.status == SettlementStatus.COMPLETED) {
        Text("이번 달 정산을 마쳤어요.", style = ChagokText.Item.copy(fontSize = 22.sp))
        return
    }
    if (m.shares.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        if (m.totalCost > 0) {
            PillButton("정산 완료", onClick = { vm.complete() })
            LinkButton("충전비 고치기", onClick = vm::startEditCost)
        } else {
            PillButton("충전비 적기", onClick = vm::startEditCost)
        }
    }
}

@Composable
private fun CostEditor(current: Int, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("이번 달 충전비", style = ChagokText.Label.copy(fontSize = 20.sp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(320.dp)) {
                TextField(
                    value = text,
                    onValueChange = { v -> text = v.filter { it.isDigit() }.take(8) },
                    placeholder = { Text("0", fontSize = 30.sp) },
                    textStyle = ChagokText.Title.copy(fontSize = 34.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Ink,
                        unfocusedIndicatorColor = Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("원", style = ChagokText.Title.copy(fontSize = 30.sp), modifier = Modifier.padding(bottom = 12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            PillButton("저장", onClick = { onSave(text) }, enabled = text.isNotBlank())
            LinkButton("취소", onClick = onCancel)
        }
    }
}
