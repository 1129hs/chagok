package com.heiseung.chagok.ui.family

import com.heiseung.chagok.ui.components.iRang
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.ui.components.CarTopView
import com.heiseung.chagok.ui.components.LinkButton
import com.heiseung.chagok.ui.components.PillButton
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Sub
import com.heiseung.chagok.ui.theme.Warn

// 06-1 사람 추가하기: 지금 시트 위치로 등록
@Composable
fun ColumnScope.AddPersonScreen(state: AddPersonUiState, vm: AddPersonViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }

    Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier.width(560.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text("누가 새로\n함께 타나요?", style = ChagokText.Hero.copy(lineHeight = 72.sp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("이름", style = ChagokText.Label)
                Box(Modifier.width(420.dp)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it.take(10) },
                        placeholder = { Text("예: 첫째", fontSize = 26.sp) },
                        textStyle = ChagokText.Title.copy(fontSize = 28.sp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Ink,
                            unfocusedIndicatorColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            val close = state.tooCloseTo
            if (close != null) {
                Text(
                    "${close.name.iRang()} 시트 위치가 비슷해요.\n평소 위치로 조금 더 맞춰주세요.",
                    style = ChagokText.Lead.copy(fontSize = 22.sp, lineHeight = 35.sp, color = Warn)
                )
            } else {
                Text(
                    "운전석을 평소처럼 맞춘 다음 눌러주세요.\n다음부터는 이 위치로 알아볼게요.",
                    style = ChagokText.Lead.copy(fontSize = 22.sp, lineHeight = 35.sp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                PillButton("이 위치로 등록하기", onClick = { vm.register(name, onDone) }, enabled = name.isNotBlank())
                LinkButton("취소", onClick = onDone)
            }
        }

        Spacer(Modifier.width(48.dp))

        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "지금 운전석 위치",
                fontSize = 20.sp,
                color = Sub,
                textAlign = TextAlign.Center
            )
            Text("${state.seatPosition}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(24.dp))
            CarTopView(Modifier.fillMaxWidth(), highlightDriverSeat = true)
        }
    }
}
