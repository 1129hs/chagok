package com.heiseung.chagok.vehicle

// 시트 앞뒤 위치는 Virtual Console에 없어서, 시연할 때 화면 위 [시트 ±10]으로 바꿈
interface SeatTestControl {
    fun setSeatPosition(value: Int)
}
