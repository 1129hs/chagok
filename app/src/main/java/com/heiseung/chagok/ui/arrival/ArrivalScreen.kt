package com.heiseung.chagok.ui.arrival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.entity.HandoverTag
import com.heiseung.chagok.ui.components.Chip
import com.heiseung.chagok.ui.components.LinkButton
import com.heiseung.chagok.ui.components.PillButton
import com.heiseung.chagok.ui.components.highlighted
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Ink

private val tagLabels = listOf(
    HandoverTag.CHARGE_NEEDED to "충전 필요해요",
    HandoverTag.LUGGAGE to "짐 있어요",
    HandoverTag.WASHED to "세차했어요",
    HandoverTag.PARKING_SPOT to "B2에 세웠어요"
)

// 03 하차: 내 운행 요약 + 다음 사람에게 한마디
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.ArrivalScreen(state: ArrivalUiState, viewModel: ArrivalViewModel, onDone: () -> Unit) {
    if (state.loading) return
    if (state.left) {
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
            Text("남겼어요.", style = ChagokText.Hero)
            Text("다음 사람이 탈 때 보여줄게요.", style = ChagokText.Lead)
        }
        return
    }
    Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("수고했어요, ${state.driverName}", style = ChagokText.Label.copy(fontSize = 20.sp))
            Text(highlighted("", "${formatKm(state.distanceKm)}km", "\n달렸어요."), style = ChagokText.Hero.copy(fontSize = 68.sp, lineHeight = 82.sp))
            Text(
                "${state.durationMin}분 동안 배터리 ${state.energyUsedPct}%를 썼어요.\n지금 ${state.endEnergyPct}% 남아 있어요.",
                style = ChagokText.Lead
            )
            Text("이번 운행은 자동으로 기록됐어요", style = ChagokText.Label)
        }
        Spacer(Modifier.width(80.dp))
        Column(Modifier.width(520.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("다음 사람에게 한마디", style = ChagokText.Title.copy(fontSize = 26.sp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tagLabels.forEach { (tag, label) ->
                    Chip(label, selected = tag in state.tags, onClick = { viewModel.toggleTag(tag) })
                }
            }
            TextField(
                value = state.message,
                onValueChange = viewModel::setMessage,
                placeholder = { Text("직접 쓰기", fontSize = 22.sp) },
                textStyle = ChagokText.Item.copy(fontSize = 24.sp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Ink,
                    unfocusedIndicatorColor = Ink
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PillButton("남기고 내리기", onClick = { viewModel.leave(onDone) })
                LinkButton("그냥 내릴게요", onClick = { viewModel.skip(onDone) })
            }
        }
    }
}

private fun formatKm(km: Double): String = if (km % 1.0 == 0.0) km.toInt().toString() else "%.1f".format(km)
