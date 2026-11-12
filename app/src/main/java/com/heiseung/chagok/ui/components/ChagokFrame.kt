package com.heiseung.chagok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Paper
import com.heiseung.chagok.ui.theme.Sub
import com.heiseung.chagok.vehicle.Gear
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class MainTab(val label: String) { HOME("홈"), HISTORY("기록"), SETTLE("정산"), FAMILY("가족") }

// 모든 화면 공통 틀: 상단 바 + 내용 + 하단 메뉴
@Composable
fun ChagokFrame(
    gear: Gear,
    driverName: String?,
    currentTab: MainTab?,
    onTab: (MainTab) -> Unit,
    onBack: (() -> Unit)? = null,
    debugActions: List<Pair<String, () -> Unit>> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 72.dp)
    ) {
        TopBar(gear, driverName, onBack, debugActions)
        Column(modifier = Modifier.weight(1f).fillMaxWidth(), content = content)
        if (currentTab != null) BottomMenu(currentTab, onTab) else Box(Modifier.height(72.dp))
    }
}

@Composable
private fun TopBar(
    gear: Gear,
    driverName: String?,
    onBack: (() -> Unit)?,
    debugActions: List<Pair<String, () -> Unit>>
) {
    var time by remember { mutableStateOf(nowText()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = nowText()
            delay(15_000)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(84.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            Text("← 돌아가기", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.clickable(onClick = onBack).padding(vertical = 12.dp))
        } else {
            Text("차곡", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            // 가짜 차일 때만 보이는 테스트 버튼
            debugActions.forEach { (label, action) ->
                Text("[$label]", fontSize = 16.sp, color = Sub, modifier = Modifier.clickable(onClick = action).padding(8.dp))
            }
            Text(
                buildAnnotatedString {
                    append(if (gear == Gear.P) "P 주차 중" else "${gear.name} 주행 중")
                    if (driverName != null) {
                        append(" · ")
                        withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.SemiBold)) { append(driverName) }
                    }
                    append(" · $time")
                },
                fontSize = 18.sp,
                color = Sub
            )
        }
    }
}

@Composable
private fun BottomMenu(current: MainTab, onTab: (MainTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(84.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        MainTab.entries.forEach { tab ->
            val selected = tab == current
            Text(
                tab.label,
                fontSize = 19.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Ink else Sub,
                modifier = Modifier.clickable { onTab(tab) }.padding(vertical = 12.dp)
            )
        }
    }
}

private fun nowText(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
