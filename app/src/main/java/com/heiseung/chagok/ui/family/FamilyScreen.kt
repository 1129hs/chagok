package com.heiseung.chagok.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.ChipLine
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Line
import com.heiseung.chagok.ui.theme.Paper
import com.heiseung.chagok.ui.theme.Sub

// 06 가족 · 설정
@Composable
fun ColumnScope.FamilyScreen(state: FamilyUiState, vm: FamilyViewModel, onAddPerson: () -> Unit) {
    if (state.loading) return
    val vehicle = state.vehicle

    Row(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp)) {
        // 왼쪽: 함께 쓰는 사람
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text("함께 쓰는 사람", style = ChagokText.Hero.copy(fontSize = 52.sp))
            Spacer(Modifier.height(28.dp))
            state.members.forEach { MemberRow(it) }
            HorizontalDivider(color = Line)
            Text(
                "+ 사람 추가하기",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 12.dp).clickable(onClick = onAddPerson).padding(vertical = 16.dp)
            )
        }

        Spacer(Modifier.width(96.dp))

        // 오른쪽: 설정
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("설정", style = ChagokText.Hero.copy(fontSize = 52.sp))
            Spacer(Modifier.height(28.dp))
            SettingRow("시트 위치로 운전자 알아보기") {
                ToggleSwitch(vehicle?.autoDetectDriver ?: true, vm::setAutoDetect)
            }
            SettingRow("탈 때 인계 메시지 보여주기") {
                ToggleSwitch(vehicle?.showHandover ?: true, vm::setShowHandover)
            }
            SettingRow("정산 기준") {
                Text("달린 거리", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            }
            HorizontalDivider(color = Line)
            Text(
                "주행 중에는 화면을 띄우지 않고 기록만 해요.",
                style = ChagokText.Label.copy(fontSize = 18.sp, lineHeight = 29.sp),
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

@Composable
private fun MemberRow(member: MemberEntity) {
    HorizontalDivider(color = Line)
    Row(
        Modifier.fillMaxWidth().height(92.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(44.dp).background(Ink, CircleShape), contentAlignment = Alignment.Center) {
                Text(member.name.take(1), color = Paper, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Text(member.name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
        Text("운전석 위치 ${member.seatPosition}로 알아봐요", fontSize = 18.sp, color = Sub)
    }
}

@Composable
private fun SettingRow(label: String, trailing: @Composable () -> Unit) {
    HorizontalDivider(color = Line)
    Row(
        Modifier.fillMaxWidth().height(92.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 22.sp, color = Ink)
        trailing()
    }
}

@Composable
private fun ToggleSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Paper,
            checkedTrackColor = Ink,
            checkedBorderColor = Ink,
            uncheckedThumbColor = Paper,
            uncheckedTrackColor = ChipLine,
            uncheckedBorderColor = ChipLine
        )
    )
}
