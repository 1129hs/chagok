package com.heiseung.chagok.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heiseung.chagok.ui.theme.ChagokText

// 아직 안 만든 화면 자리
@Composable
fun ColumnScope.PlaceholderScreen(title: String) {
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("$title 화면 (준비 중)", style = ChagokText.Title)
    }
}
