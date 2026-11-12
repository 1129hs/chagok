package com.heiseung.chagok.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.heiseung.chagok.ui.theme.ChagokText
import com.heiseung.chagok.ui.theme.ChipLine
import com.heiseung.chagok.ui.theme.Highlight
import com.heiseung.chagok.ui.theme.Ink
import com.heiseung.chagok.ui.theme.Paper

// 검정 알약 버튼
@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(68.dp).widthIn(min = 260.dp),
        shape = RoundedCornerShape(34.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper)
    ) { Text(text, style = ChagokText.Button) }
}

// 밑줄 텍스트 버튼
@Composable
fun LinkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.height(68.dp)) {
        Text(text, style = ChagokText.Button, color = Ink, textDecoration = TextDecoration.Underline)
    }
}

// 03 하차 빠른 한마디 칩
@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(2.dp, if (selected) Ink else ChipLine),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Ink else Paper,
            contentColor = if (selected) Paper else Ink
        ),
        contentPadding = PaddingValues(horizontal = 22.dp)
    ) { Text(text, style = ChagokText.Button.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)) }
}

// 문장 안 일부만 노란 형광펜
fun highlighted(before: String, mark: String, after: String = ""): AnnotatedString = buildAnnotatedString {
    append(before)
    withStyle(SpanStyle(background = Highlight, color = Ink, fontWeight = FontWeight.Bold)) { append(mark) }
    append(after)
}
