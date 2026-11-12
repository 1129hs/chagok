package com.heiseung.chagok.ui.components

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// 12.0 → "12km", 12.4 → "12.4km"
fun km(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()}km" else "${rounded}km"
}

// 84000 → "84,000원"
fun won(value: Int): String = NumberFormat.getNumberInstance(Locale.KOREA).format(value) + "원"

// 이름 뒤 조사: 받침 있으면 "혜승이", 없으면 "엄마가"
private fun hasBatchim(word: String): Boolean {
    val last = word.lastOrNull() ?: return false
    if (last !in '가'..'힣') return false
    return (last - '가') % 28 != 0
}

fun String.iGa(): String = this + if (hasBatchim(this)) "이" else "가"
fun String.iRang(): String = this + if (hasBatchim(this)) "이랑" else "랑"
fun String.ieyo(): String = this + if (hasBatchim(this)) "이에요" else "예요"
