package com.example.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View

object ScreenCaptureHelper {

    /**
     * Chụp ảnh màn hình tiêu chuẩn (PixelCopy từ Window/SurfaceFlinger trên Android 8.0+).
     * Bắt chính xác toàn bộ những gì người dùng đang nhìn thấy trên màn hình:
     * - Không bị lỗi màn hình đen / màn hình trắng khi WebView bật tăng tốc phần cứng.
     * - Bắt được cả thanh địa chỉ, giao diện web, video, canvas... y hệt như chụp màn hình điện thoại.
     * - Nếu có lỗi PixelCopy sẽ tự động fallback sang vẽ View.
     */
    fun captureActivityScreenshot(activity: Activity?, onResult: (Bitmap?) -> Unit) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onResult(null)
            return
        }

        val window = activity.window ?: run {
            onResult(null)
            return
        }

        val decorView = window.decorView.rootView
        val width = decorView.width
        val height = decorView.height

        if (width <= 0 || height <= 0) {
            // View chưa đo đạc xong, thử lại với post
            decorView.post {
                val w = decorView.width
                val h = decorView.height
                if (w > 0 && h > 0) {
                    performCapture(activity, decorView, w, h, onResult)
                } else {
                    onResult(null)
                }
            }
            return
        }

        performCapture(activity, decorView, width, height, onResult)
    }

    private fun performCapture(
        activity: Activity,
        decorView: View,
        width: Int,
        height: Int,
        onResult: (Bitmap?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val location = IntArray(2)
            decorView.getLocationInWindow(location)
            val rect = Rect(location[0], location[1], location[0] + width, location[1] + height)

            try {
                PixelCopy.request(
                    activity.window,
                    rect,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            onResult(bitmap)
                        } else {
                            // Fallback to view drawing
                            onResult(captureViewFallback(decorView))
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
                return
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        // Fallback for older APIs or if PixelCopy threw exception
        onResult(captureViewFallback(decorView))
    }

    private fun captureViewFallback(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}
