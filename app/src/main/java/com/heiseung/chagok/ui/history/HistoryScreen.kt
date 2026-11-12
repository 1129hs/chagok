package com.heiseung.chagok.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.dao.TripListItem
import com.heiseung.chagok.ui.components.km
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Line
import com.heiseung.chagok.ui.theme.Sub
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// 05 운행 기록: 날짜별로 누가 얼마나 탔는지
@Composable
fun ColumnScope.HistoryScreen(state: HistoryUiState, vm: HistoryViewModel) {
    if (state.loading) return

    Column(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("운행 기록", style = ChagokText.Hero.copy(fontSize = 52.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                FilterTab("전체", selected = state.filter == null) { vm.select(null) }
                state.members.forEach { m -> FilterTab(m.name, selected = state.filter == m.id) { vm.select(m.id) } }
            }
        }

        if (state.trips.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text("이번 달 운행 기록이 아직 없어요.", style = ChagokText.Lead.copy(fontSize = 28.sp))
            }
            return@Column
        }

        val groups = state.trips.groupBy { dayOf(it.startedAt) }
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            groups.forEach { (day, trips) ->
                item(key = "day-$day") {
                    Text(
                        dayLabel(day),
                        style = ChagokText.Label,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(trips, key = { it.tripId }) { trip -> TripRow(trip) }
            }
            item { HorizontalDivider(color = Line) }
        }
    }
}

@Composable
private fun FilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 20.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Ink else Sub,
        modifier = Modifier
            .clickable(onClick = onClick)
            .drawBehind {
                // 고른 탭만 밑줄
                if (selected) {
                    val y = size.height - 1.5.dp.toPx()
                    drawLine(Ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 3.dp.toPx())
                }
            }
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun TripRow(trip: TripListItem) {
    Column {
        HorizontalDivider(color = Line)
        Row(Modifier.fillMaxWidth().height(76.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(trip.memberName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.width(110.dp))
            Text(
                buildAnnotatedString {
                    append(routeText(trip))
                    if (trip.hasHandover) {
                        withStyle(SpanStyle(fontSize = 17.sp, color = Sub)) { append("  · 메모 남김") }
                    }
                },
                fontSize = 22.sp,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(timeRange(trip), fontSize = 19.sp, color = Sub, modifier = Modifier.width(200.dp))
            Text(
                km(trip.distanceKm ?: 0.0),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                textAlign = TextAlign.End,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

private val zone: ZoneId get() = ZoneId.systemDefault()
private val hm = DateTimeFormatter.ofPattern("HH:mm")

private fun dayOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

private fun dayLabel(day: LocalDate): String {
    val today = LocalDate.now()
    return when (day) {
        today -> "오늘"
        today.minusDays(1) -> "어제"
        else -> day.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
    }
}

private fun timeRange(trip: TripListItem): String {
    val start = Instant.ofEpochMilli(trip.startedAt).atZone(zone).format(hm)
    val end = trip.endedAt?.let { Instant.ofEpochMilli(it).atZone(zone).format(hm) } ?: "–"
    return "$start – $end"
}

// 출발지·도착지는 내비 연동 후 채워짐, 없으면 운행 시간만
private fun routeText(trip: TripListItem): String {
    val from = trip.originName
    val to = trip.destinationName
    return when {
        from != null && to != null -> "$from → $to"
        to != null -> "→ $to"
        else -> {
            val min = ((trip.endedAt ?: trip.startedAt) - trip.startedAt) / 60_000L
            "${min}분 운행"
        }
    }
}
