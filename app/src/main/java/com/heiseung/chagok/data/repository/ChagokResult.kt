package com.heiseung.chagok.data.repository

// 서버 API 명세의 에러 코드와 같은 이름
enum class ChagokError {
    MEMBER_NOT_FOUND,
    TRIP_NOT_FOUND,
    TRIP_ALREADY_DRIVING,
    TRIP_NOT_DRIVING,
    HANDOVER_ALREADY_EXISTS,
    HANDOVER_ALREADY_CONFIRMED,
    SETTLEMENT_COMPLETED,
    INVALID_SEAT_POSITION
}

sealed class ChagokResult<out T> {
    data class Success<T>(val data: T) : ChagokResult<T>()
    data class Failure(val error: ChagokError) : ChagokResult<Nothing>()
}
