package com.heiseung.chagok.ai

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

// 하차 화면 "말로 남기기" 녹음 (AAC, Gemini가 바로 받는 형식)
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private val file: File get() = File(context.cacheDir, "handover.aac")

    fun start() {
        cancel()
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioSamplingRate(16_000)
        r.setAudioEncodingBitRate(64_000)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
    }

    // 녹음 끝 → 파일, 너무 짧아서 실패하면 null
    fun stop(): File? {
        val r = recorder ?: return null
        recorder = null
        return try {
            r.stop()
            file
        } catch (e: RuntimeException) {
            null
        } finally {
            r.release()
        }
    }

    // 마지막으로 부른 뒤 가장 큰 소리 크기 (0이면 마이크로 아무 소리도 안 들어옴)
    fun peakAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (e: RuntimeException) {
        0
    }

    fun cancel() {
        val r = recorder ?: return
        recorder = null
        try {
            r.stop()
        } catch (e: RuntimeException) {
            // 시작 직후 멈추면 예외가 나는데 버리는 녹음이라 무시
        }
        r.release()
    }
}
