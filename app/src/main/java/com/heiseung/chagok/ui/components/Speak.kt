package com.heiseung.chagok.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

// 화면에 들어오면 한 번 소리 내 읽기 (인계 메시지), 화면을 떠나면 멈춤
@Composable
fun SpeakOnce(text: String) {
    val context = LocalContext.current
    DisposableEffect(text) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "handover")
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
