package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object OnDeviceAiHelper {

    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    private const val GEMINI_PACKAGE = "com.google.android.apps.bard"
    private const val LENS_PACKAGE = "com.google.ar.lens"
    private const val GOOGLE_SEARCH_PACKAGE = "com.google.android.googlequicksearchbox"

    /**
     * Lưu Bitmap vào cache file nội bộ (không lưu vào bộ nhớ máy/thư viện ảnh)
     * Trả về content:// URI hợp lệ qua FileProvider để chia sẻ sang các ứng dụng khác.
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, "screenshots")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val cacheFile = File(cacheFolder, "screen_capture.jpg")
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            val authority = "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX"
            FileProvider.getUriForFile(context, authority, cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Kiểm tra ứng dụng Gemini đã được cài trên máy hay chưa.
     */
    fun isGeminiAppInstalled(context: Context): Boolean {
        return isPackageInstalled(context, GEMINI_PACKAGE)
    }

    /**
     * Kiểm tra ứng dụng Google Lens (hoặc Google app có tích hợp Lens) đã cài trên máy hay chưa.
     */
    fun isGoogleLensAppInstalled(context: Context): Boolean {
        return isPackageInstalled(context, LENS_PACKAGE) || isPackageInstalled(context, GOOGLE_SEARCH_PACKAGE)
    }

    /**
     * Mở trực tiếp ứng dụng Gemini trên máy với ảnh đã chụp để giải bài / tìm kiếm.
     * Hoàn toàn MIỄN PHÍ - KHÔNG TỐN LƯỢT DÙNG API KEY CỦA NGƯỜI DÙNG!
     */
    fun openWithGeminiApp(context: Context, bitmap: Bitmap): Boolean {
        val uri = saveBitmapToCache(context, bitmap)
        if (uri == null) {
            Toast.makeText(context, "Không thể chuẩn bị ảnh chụp", Toast.LENGTH_SHORT).show()
            return false
        }

        // 1. Thử mở app Gemini chính thức
        val geminiIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Hãy giải bài tập này hoặc giải thích chi tiết nội dung trong hình ảnh")
            setPackage(GEMINI_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (isIntentResolvable(context, geminiIntent)) {
            try {
                context.startActivity(geminiIntent)
                Toast.makeText(context, "Đang mở Gemini trên điện thoại...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Thử mở qua app Google Search / Trợ lý
        val googleIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Hãy giải bài tập này hoặc giải thích chi tiết nội dung trong hình ảnh")
            setPackage(GOOGLE_SEARCH_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (isIntentResolvable(context, googleIntent)) {
            try {
                context.startActivity(googleIntent)
                Toast.makeText(context, "Đang mở Trợ lý Google / Gemini...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Nếu máy chưa cài app Gemini riêng, mở bảng chọn ứng dụng AI để người dùng chọn
        Toast.makeText(context, "Máy bạn chưa cài app Gemini riêng. Đang mở danh sách AI trên máy...", Toast.LENGTH_LONG).show()
        shareToAnyAiApp(context, bitmap, "Hãy giải bài tập này hoặc phân tích nội dung trong ảnh")
        return true
    }

    /**
     * Mở trực tiếp ứng dụng Google Lens trên máy với ảnh đã chụp để quét chữ / giải bài.
     * Hoàn toàn MIỄN PHÍ - KHÔNG TỐN LƯỢT DÙNG API!
     */
    fun openWithGoogleLensApp(context: Context, bitmap: Bitmap): Boolean {
        val uri = saveBitmapToCache(context, bitmap)
        if (uri == null) {
            Toast.makeText(context, "Không thể chuẩn bị ảnh chụp", Toast.LENGTH_SHORT).show()
            return false
        }

        // 1. Thử mở app Google Lens độc lập qua ACTION_VIEW
        val lensViewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            setPackage(LENS_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (isIntentResolvable(context, lensViewIntent)) {
            try {
                context.startActivity(lensViewIntent)
                Toast.makeText(context, "Đang mở Google Lens trên máy...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Thử mở qua ACTION_SEND vào Google Lens
        val lensSendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(LENS_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (isIntentResolvable(context, lensSendIntent)) {
            try {
                context.startActivity(lensSendIntent)
                Toast.makeText(context, "Đang mở Google Lens trên máy...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Thử qua app Google Search
        val googleSearchLensIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage(GOOGLE_SEARCH_PACKAGE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (isIntentResolvable(context, googleSearchLensIntent)) {
            try {
                context.startActivity(googleSearchLensIntent)
                Toast.makeText(context, "Đang mở Google Lens...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Nếu máy chưa có Lens, cho phép chọn bất kỳ AI hoặc ứng dụng nào có trên máy
        shareToAnyAiApp(context, bitmap, "Tìm kiếm và giải bài với Google Lens")
        return true
    }

    /**
     * Chia sẻ ảnh chụp cho bất kỳ ứng dụng AI nào đã cài trên máy (ChatGPT, Copilot, Claude, Zalo, QuickShare, etc.)
     * Tuyệt đối không tiêu tốn API token!
     */
    fun shareToAnyAiApp(context: Context, bitmap: Bitmap, prompt: String = "Giải bài tập / Tìm kiếm nội dung ảnh này") {
        val uri = saveBitmapToCache(context, bitmap) ?: return
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, prompt)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Chọn AI trên điện thoại (Gemini, Lens, ChatGPT, Copilot...)").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Không tìm thấy ứng dụng phù hợp để chia sẻ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (t: Throwable) {
            false
        }
    }

    private fun isIntentResolvable(context: Context, intent: Intent): Boolean {
        return try {
            intent.resolveActivity(context.packageManager) != null
        } catch (t: Throwable) {
            false
        }
    }
}
