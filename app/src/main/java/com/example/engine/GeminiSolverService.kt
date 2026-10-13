package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiSolverService {

    private const val PREFS_NAME = "gemini_prefs"
    private const val PREF_CUSTOM_API_KEY = "custom_gemini_api_key"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(PREF_CUSTOM_API_KEY, "")?.trim()
        if (!customKey.isNullOrEmpty()) {
            return customKey
        }
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    suspend fun solveExerciseOrSearch(
        context: Context,
        prompt: String,
        pageContext: String? = null,
        mode: String = "SOLVE" // "SOLVE", "SEARCH", "SUMMARIZE", "EXPLAIN"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình API Key Gemini. Vui lòng bấm vào biểu tượng cài đặt (bánh răng) ở trên để nhập API Key của bạn hoặc cài đặt trong Secrets.")
            )
        }

        try {
            val systemPrompt = when (mode) {
                "SOLVE" -> "Bạn là Trợ lý Học tập & Giải Bài Tập Nhật Anh AI (sử dụng Gemini 3.5 Flash). Hãy giải chi tiết từng bước, rõ ràng, chính xác cho bài tập/câu hỏi được cung cấp. Nêu rõ: Phương pháp giải, Các bước thực hiện, và Đáp án / Kết luận cuối cùng."
                "SUMMARIZE" -> "Bạn là Trợ lý Tóm tắt Nhật Anh AI. Hãy tóm tắt nội dung chính ngắn gọn, súc tích bằng các gạch đầu dòng rõ ràng."
                "EXPLAIN" -> "Bạn là Trợ lý Giải Thích Khái Niệm Nhật Anh AI. Hãy giải thích dễ hiểu, minh họa bằng ví dụ thực tế."
                else -> "Bạn là Trợ lý Trình duyệt Nhật Anh AI. Hãy tìm kiếm và trả lời thắc mắc của người dùng chính xác, súc tích và hữu ích."
            }

            val finalUserContent = buildString {
                if (!pageContext.isNullOrBlank()) {
                    appendLine("--- NGỮ CẢNH TRANG WEB ĐANG XEM ---")
                    appendLine(pageContext.take(2000))
                    appendLine("-----------------------------------")
                    appendLine()
                }
                appendLine("YÊU CẦU: $prompt")
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", finalUserContent)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("topP", 0.95)
                })
            }

            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: "Lỗi HTTP ${response.code}"
                } catch (e: Exception) {
                    "Lỗi HTTP ${response.code}: $responseString"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonObject = JSONObject(responseString)
            val candidates = jsonObject.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                Result.failure(Exception("Gemini không trả về nội dung phù hợp."))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
