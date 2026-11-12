package com.heiseung.chagok.ui.boarding

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heiseung.chagok.data.local.entity.MemberEntity
import com.heiseung.chagok.ui.components.CarTopView
import com.heiseung.chagok.ui.components.LinkButton
import com.heiseung.chagok.ui.components.PillButton
import com.heiseung.chagok.ui.components.highlighted
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.Ink

// 01 탑승: 시트 위치로 운전자 확인
@Composable
fun ColumnScope.BoardingScreen(state: BoardingUiState, viewModel: BoardingViewModel, onConfirmed: () -> Unit) {
    if (state.loading) return
    Row(modifier = Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            var registering by remember { mutableStateOf(false) }
            when {
                state.members.isEmpty() -> NewMember(viewModel, onConfirmed, first = true, onCancel = null)
                registering -> NewMember(viewModel, onConfirmed, first = false, onCancel = { registering = false })
                state.matched != null -> Matched(state.matched, state.members, viewModel, onConfirmed)
                else -> PickMember(state.members, viewModel, onConfirmed, onNew = { registering = true })
            }
        }
        Spacer(Modifier.width(48.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("운전석 위치 ${state.seatPosition}", style = ChagokText.Item)
            Spacer(Modifier.height(16.dp))
            CarTopView(modifier = Modifier.fillMaxWidth(), highlightDriverSeat = true)
        }
    }
}

@Composable
private fun Matched(member: MemberEntity, members: List<MemberEntity>, vm: BoardingViewModel, onConfirmed: () -> Unit) {
    Text("안녕하세요", style = ChagokText.Label.copy(fontSize = 20.sp))
    Text(highlighted("${member.name}, ", "맞죠?"), style = ChagokText.Hero)
    Text("운전석 시트 위치를 보고 알아봤어요.", style = ChagokText.Lead)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        PillButton("네, 맞아요", onClick = { vm.confirm(member); onConfirmed() })
        members.filter { it.id != member.id }.forEach { other ->
            LinkButton("${other.name}예요", onClick = { vm.confirm(other); onConfirmed() })
        }
    }
}

@Composable
private fun PickMember(members: List<MemberEntity>, vm: BoardingViewModel, onConfirmed: () -> Unit, onNew: () -> Unit) {
    Text("누가 운전하나요?", style = ChagokText.Hero)
    Text("시트 위치로는 잘 모르겠어요.", style = ChagokText.Lead)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        members.forEach { m -> PillButton(m.name, onClick = { vm.confirm(m); onConfirmed() }) }
        LinkButton("처음 타는 사람이에요", onClick = onNew)
    }
}

@Composable
private fun NewMember(vm: BoardingViewModel, onConfirmed: () -> Unit, first: Boolean, onCancel: (() -> Unit)?) {
    var name by remember { mutableStateOf("") }
    Text(if (first) "처음 오셨네요" else "새로 함께 타는 사람", style = ChagokText.Label.copy(fontSize = 20.sp))
    Text("누가 운전하나요?", style = ChagokText.Hero)
    Text("평소 앉는 대로 시트를 맞춘 다음 이름을 적어주세요.", style = ChagokText.Lead)
    Box(Modifier.width(420.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("이름", fontSize = 24.sp) },
            textStyle = ChagokText.Title.copy(fontSize = 26.sp),
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
    Box(Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PillButton("이 위치로 등록하기", onClick = { vm.register(name) { onConfirmed() } }, enabled = name.isNotBlank())
            if (onCancel != null) LinkButton("취소", onClick = onCancel)
        }
    }
}
