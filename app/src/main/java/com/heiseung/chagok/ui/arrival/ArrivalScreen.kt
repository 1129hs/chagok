package com.heiseung.chagok.ui.arrival

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.heiseung.chagok.ui.theme.ChipLine
import com.heiseung.chagok.ui.theme.Paper
import com.heiseung.chagok.ui.theme.Warn
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
            VoiceButton(state.voice, viewModel)
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
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Ink,
                    unfocusedIndicatorColor = Ink
                ),
                modifier = Modifier.fillMaxWidth()
            )
            state.heard?.let { Text("들은 말: $it", style = ChagokText.Label, maxLines = 2) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PillButton("남기고 내리기", onClick = { viewModel.leave(onDone) })
                LinkButton("그냥 내릴게요", onClick = { viewModel.skip(onDone) })
            }
        }
    }
}

// 말로 남기기: 누르면 녹음, 다시 누르면 AI가 정리
@Composable
private fun VoiceButton(voice: VoiceState, viewModel: ArrivalViewModel) {
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startVoice()
    }
    val recording = voice == VoiceState.RECORDING
    val label = when (voice) {
        VoiceState.RECORDING -> "■  다 말했어요"
        VoiceState.THINKING -> "정리하는 중…"
        else -> "🎤  말로 남기기"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = {
                when (voice) {
                    VoiceState.RECORDING -> viewModel.finishVoice()
                    VoiceState.THINKING -> Unit
                    else -> {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) viewModel.startVoice() else permission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            enabled = voice != VoiceState.THINKING,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, if (recording) Ink else ChipLine),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (recording) Ink else Paper,
                contentColor = if (recording) Paper else Ink
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) { Text(label, style = ChagokText.Button) }

        when (voice) {
            VoiceState.RECORDING -> Text("듣고 있어요. 다 말하면 한 번 더 눌러주세요.", style = ChagokText.Label)
            VoiceState.FAILED -> Text("잘 못 들었어요. 다시 말하거나 아래에 직접 써주세요.", style = ChagokText.Label.copy(color = Warn))
            VoiceState.NO_SOUND -> Text("마이크 소리가 안 들어와요. 아래에 직접 써주세요.", style = ChagokText.Label.copy(color = Warn))
            else -> Unit
        }
    }
}

private fun formatKm(km: Double): String = if (km % 1.0 == 0.0) km.toInt().toString() else "%.1f".format(km)
