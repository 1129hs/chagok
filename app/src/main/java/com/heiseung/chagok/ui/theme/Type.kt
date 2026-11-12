package com.heiseung.chagok.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography()

// 화면에서 직접 쓰는 글자 크기 (디자인 기준, 실제 화면 보고 조정)
object ChagokText {
    val Hero = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 70.sp, letterSpacing = (-1.5).sp, color = Ink)
    val Title = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp, letterSpacing = (-0.8).sp, color = Ink)
    val Lead = TextStyle(fontSize = 24.sp, lineHeight = 38.sp, color = Body)
    val Label = TextStyle(fontSize = 17.sp, color = Sub)
    val Item = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
    val Button = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
}
