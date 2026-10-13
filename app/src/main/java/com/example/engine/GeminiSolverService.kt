package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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

    /**
     * Text-based question solving and in-browser search
     */
    suspend fun solveExerciseOrSearch(
        context: Context,
        prompt: String,
        pageContext: String? = null,
        mode: String = "SOLVE" // "SOLVE", "SEARCH", "SUMMARIZE", "EXPLAIN"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình API Key Gemini. Vui lòng bấm vào biểu tượng cài đặt để nhập API Key của bạn.")
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

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }
            Result.failure(Exception("Không nhận được nội dung trả về từ Gemini API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Multimodal Image Analysis for Google Lens (OCR, Exercise Solving & Visual Search)
     */
    suspend fun analyzeImageWithGemini(
        context: Context,
        bitmap: Bitmap,
        prompt: String = "",
        mode: String = "SOLVE" // "SOLVE", "OCR", "SEARCH"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình API Key Gemini. Vui lòng bấm vào biểu tượng cài đặt để nhập API Key của bạn.")
            )
        }

        try {
            val base64Image = compressBitmapToBase64(bitmap)

            val systemPrompt = when (mode) {
                "OCR" -> "Bạn là tính năng Google Lens Trích Xuất Chữ (OCR) của Nhật Anh Browser. Nhiệm vụ của bạn là đọc và trích xuất TOÀN BỘ chữ viết, văn bản, số liệu, ký hiệu toán học trong ảnh một cách chính xác tuyệt đối. Định dạng sạch sẽ, giữ nguyên ngắt dòng theo ảnh để người dùng sao chép văn bản dễ nhất. Không thêm câu chào mở đầu hay kết thúc."
                "SOLVE" -> "Bạn là tính năng Google Lens Giải Bài Tập của Nhật Anh Browser (Gemini 3.5 Flash). Hãy đọc kỹ nội dung bài tập/câu hỏi trong ảnh chụp màn hình trình duyệt. Cung cấp: 1. Đề bài nhận diện được. 2. Phương pháp giải rõ ràng. 3. Lời giải chi tiết từng bước. 4. ĐÁP ÁN CHÍNH XÁC (in đậm, nổi bật)."
                "SEARCH" -> "Bạn là Google Lens Tìm Kiếm & Nhận Diện của Nhật Anh Browser. Hãy phân tích hình ảnh, giải thích nội dung, nguồn gốc, chủ đề và cung cấp các thông tin liên quan hữu ích nhất."
                else -> "Bạn là Trợ lý Google Lens Nhật Anh AI. Hãy phân tích bức ảnh và giải đáp câu hỏi của người dùng một cách chính xác."
            }

            val userInstruction = prompt.ifBlank {
                when (mode) {
                    "OCR" -> "Trích xuất toàn bộ chữ trong hình ảnh này."
                    "SOLVE" -> "Nhận diện và giải chi tiết bài tập trong hình ảnh này, đưa ra đáp án."
                    "SEARCH" -> "Tìm kiếm và giải thích nội dung trong hình ảnh này."
                    else -> "Phân tích bức ảnh này."
                }
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            // 1. Inline image data
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                            // 2. Text instructions
                            put(JSONObject().apply {
                                put("text", userInstruction)
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
                    put("temperature", 0.3)
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

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }
            Result.failure(Exception("Không nhận được nội dung trả về từ Google Lens Gemini API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun compressBitmapToBase64(bitmap: Bitmap, maxDim: Int = 1280): String {
        val width = bitmap.width
        val height = bitmap.height
        val scaledBitmap = if (width > maxDim || height > maxDim) {
            val scale = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
            val targetW = (width * scale).toInt().coerceAtLeast(1)
            val targetH = (height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
