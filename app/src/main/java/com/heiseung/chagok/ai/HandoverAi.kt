package com.heiseung.chagok.ai

import android.util.Base64
import android.util.Log
import com.heiseung.chagok.data.local.entity.HandoverTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class GeminiHttpException(val code: Int, body: String) : IOException("Gemini HTTP $code: $body")

// 음성 인계 결과
data class VoiceHandover(
    val transcript: String,
    val message: String,
    val tags: List<HandoverTag>
)

// 녹음 파일을 Gemini에 보내서 받아쓰기 + 한두 문장 요약 + 태그를 한 번에 받음
class HandoverAi(private val apiKey: String) {

    suspend fun summarize(audio: File): Result<VoiceHandover> = withContext(Dispatchers.IO) {
        runCatching {
            if (apiKey.isBlank()) throw IOException("GEMINI_API_KEY가 비어 있음")
            val bytes = audio.readBytes()
            Log.d(TAG, "녹음 크기 ${bytes.size} bytes")
            val audioBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            parse(postWithRetry(requestBody(audioBase64)))
        }.onFailure { Log.w(TAG, "음성 인계 실패", it) }
    }

    private fun requestBody(audioBase64: String): String {
        val parts = JSONArray()
            .put(JSONObject().put("inline_data", JSONObject().put("mime_type", "audio/aac").put("data", audioBase64)))
            .put(JSONObject().put("text", PROMPT))

        // 답을 정해진 JSON 모양으로만 받기
        val schema = JSONObject()
            .put("type", "OBJECT")
            .put(
                "properties", JSONObject()
                    .put("transcript", JSONObject().put("type", "STRING"))
                    .put("message", JSONObject().put("type", "STRING"))
                    .put(
                        "tags", JSONObject()
                            .put("type", "ARRAY")
                            .put("items", JSONObject().put("type", "STRING").put("enum", JSONArray(HandoverTag.entries.map { it.name })))
                    )
            )
            .put("required", JSONArray(listOf("transcript", "message", "tags")))

        return JSONObject()
            .put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            .put(
                "generationConfig", JSONObject()
                    .put("responseMimeType", "application/json")
                    .put("responseSchema", schema)
                    .put("temperature", 0.2)
            )
            .toString()
    }

    // 서버가 바쁘면(503 등) 잠깐 쉬고 한 번 더, 응답이 너무 늦거나 그래도 안 되면 다음 모델로
    private suspend fun postWithRetry(body: String): String {
        var last: Exception? = null
        for (model in MODELS) {
            for (attempt in 1..2) {
                try {
                    return post(model, body)
                } catch (e: GeminiHttpException) {
                    last = e
                    if (e.code !in RETRY_CODES) throw e
                    Log.w(TAG, "$model 바쁨(${e.code}), 시도 $attempt")
                    delay(1_000L * attempt)
                } catch (e: SocketTimeoutException) {
                    last = e
                    Log.w(TAG, "$model 응답 없음, 다음 모델로")
                    break
                }
            }
        }
        throw last ?: IOException("Gemini 호출 실패")
    }

    private fun post(model: String, body: String): String {
        val conn = URL("$BASE_URL/$model:generateContent").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            if (code !in 200..299) {
                val error = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw GeminiHttpException(code, error.take(300))
            }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(response: String): VoiceHandover {
        val text = JSONObject(response)
            .getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
            .getString("text")
        Log.d(TAG, "Gemini 응답: $text")
        val json = JSONObject(text)
        val tagArray = json.optJSONArray("tags") ?: JSONArray()
        val tags = (0 until tagArray.length()).mapNotNull { i ->
            runCatching { HandoverTag.valueOf(tagArray.getString(i)) }.getOrNull()
        }
        return VoiceHandover(
            transcript = json.optString("transcript").trim(),
            message = json.optString("message").trim(),
            tags = tags.distinct()
        )
    }

    companion object {
        private const val TAG = "HandoverAi"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        // 빠른 Flash-Lite 먼저 → 안 되면 Flash
        private val MODELS = listOf("gemini-flash-lite-latest", "gemini-flash-latest")
        // 잠깐 기다리면 풀리는 에러: 요청 많음, 서버 오류, 과부하
        private val RETRY_CODES = setOf(429, 500, 503)

        private val PROMPT = """
            공유 차량 앱에서 운전자가 내리면서 다음 운전자에게 남긴 음성이야.
            듣고 아래 형식의 JSON으로만 답해.
            - transcript: 들은 말 그대로 받아쓰기
            - message: 다음 사람에게 보여줄 말. 1~2문장, "~요"체, 40자 이내. 인사말·군더더기는 빼고 할 일과 상태만.
            - tags: 내용에 해당하는 것만 고르기
              CHARGE_NEEDED = 충전이 필요함
              LUGGAGE = 차 안이나 트렁크에 짐·물건이 있음
              WASHED = 세차를 했음
              PARKING_SPOT = 지하 2층(B2)에 주차함
            말소리가 없거나 알아들을 수 없으면 transcript와 message를 빈 문자열로.
        """.trimIndent()
    }
}
