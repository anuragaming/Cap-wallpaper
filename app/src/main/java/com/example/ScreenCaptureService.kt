package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "com.example.snapshare.ACTION_START"
        const val ACTION_STOP = "com.example.snapshare.ACTION_STOP"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"

        private const val SERVICE_NOTIFICATION_ID = 1001
        private const val SERVICE_CHANNEL_ID = "snapshare_service_channel"
        private const val CAPTURE_CHANNEL_ID = "snapshare_capture_channel"

        var isRunning = false
            private set

        var activeService: ScreenCaptureService? = null
            private set

        fun stopService(context: Context) {
            val intent = Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageHandlerThread: HandlerThread? = null
    private var imageHandler: Handler? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        activeService = this
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        imageHandlerThread = HandlerThread("ImageReaderThread").apply { start() }
        imageHandler = Handler(imageHandlerThread!!.looper)

        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }

                if (resultCode != 0 && data != null) {
                    startForegroundServiceWithNotification()
                    initMediaProjectionAndDisplay(resultCode, data)
                } else {
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val stopIntent = Intent(this, ShareBroadcastReceiver::class.java).apply {
            action = ShareBroadcastReceiver.ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            101,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_camera_overlay)
            .setContentTitle(getString(R.string.service_running_title))
            .setContentText(getString(R.string.service_running_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_close, getString(R.string.action_stop), stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(SERVICE_NOTIFICATION_ID, notification)
        }
    }

    private fun initMediaProjectionAndDisplay(resultCode: Int, data: Intent) {
        try {
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
            if (mediaProjection == null) {
                Toast.makeText(this, "Không thể khởi tạo MediaProjection", Toast.LENGTH_SHORT).show()
                stopSelf()
                return
            }

            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    cleanupProjection()
                    stopSelf()
                }
            }, Handler(Looper.getMainLooper()))

            setupVirtualDisplay()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun setupVirtualDisplay() {
        val projection = mediaProjection ?: return
        val (width, height, densityDpi) = getScreenDimensions()

        try {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3)
            imageReader?.setOnImageAvailableListener({ reader ->
                // Drain any non-needed frames to keep the reader active
                try {
                    val img = reader.acquireLatestImage()
                    img?.close()
                } catch (e: Exception) {
                    // ignore
                }
            }, imageHandler)

            val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
            virtualDisplay = projection.createVirtualDisplay(
                "SnapShareCapture",
                width,
                height,
                densityDpi,
                flags,
                imageReader?.surface,
                object : VirtualDisplay.Callback() {
                    override fun onPaused() {}
                    override fun onResumed() {}
                    override fun onStopped() {
                        virtualDisplay = null
                    }
                },
                imageHandler
            )

            if (virtualDisplay == null) {
                Toast.makeText(this, "Không thể tạo VirtualDisplay", Toast.LENGTH_LONG).show()
                stopSelf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi thiết lập VirtualDisplay: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    suspend fun captureFrame(): Bitmap? {
        val reader = imageReader ?: return null
        return withContext(Dispatchers.Default) {
            extractBitmapFromReader(reader)
        }
    }

    private suspend fun extractBitmapFromReader(reader: ImageReader): Bitmap? {
        var image: Image? = null
        for (attempt in 1..8) {
            try {
                image = reader.acquireLatestImage()
                if (image != null) break
            } catch (e: Exception) {
                // Retry if buffer not ready
            }
            delay(40)
        }

        if (image == null) return null

        return try {
            val width = image.width
            val height = image.height
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            val rawBitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )
            rawBitmap.copyPixelsFromBuffer(buffer)

            val resultBitmap = if (rowPadding != 0) {
                val cropped = Bitmap.createBitmap(rawBitmap, 0, 0, width, height)
                rawBitmap.recycle()
                cropped
            } else {
                rawBitmap
            }
            resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                image.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun getScreenDimensions(): Triple<Int, Int, Int> {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        defaultDisplay?.getRealMetrics(metrics)

        var width = metrics.widthPixels
        var height = metrics.heightPixels
        var densityDpi = metrics.densityDpi

        if (width <= 0 || height <= 0) {
            val dm = resources.displayMetrics
            width = dm.widthPixels
            height = dm.heightPixels
            densityDpi = dm.densityDpi
        }

        if (densityDpi <= 0) {
            densityDpi = DisplayMetrics.DENSITY_DEFAULT
        }

        return Triple(width, height, densityDpi)
    }

    fun showScreenshotNotification(file: File, bitmap: Bitmap) {
        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        // Action 1: Copy to clipboard shortcut
        val copyIntent = Intent(this, ShareActivity::class.java).apply {
            putExtra(ShareActivity.EXTRA_FILE_PATH, file.absolutePath)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val copyPendingIntent = PendingIntent.getActivity(
            this,
            notificationId + 1,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Delete
        val deleteIntent = Intent(this, ShareBroadcastReceiver::class.java).apply {
            action = ShareBroadcastReceiver.ACTION_DELETE
            putExtra(ShareBroadcastReceiver.EXTRA_FILE_PATH, file.absolutePath)
            putExtra(ShareBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 2,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previewBitmap = try {
            val maxDimension = 512
            val width = bitmap.width
            val height = bitmap.height
            val scale = (maxDimension.toFloat() / Math.max(width, height)).coerceAtMost(1f)
            Bitmap.createScaledBitmap(
                bitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } catch (e: Exception) {
            bitmap
        }

        val notification = NotificationCompat.Builder(this, CAPTURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_camera_overlay)
            .setContentTitle(getString(R.string.screenshot_captured))
            .setContentText(getString(R.string.screenshot_captured_desc))
            .setLargeIcon(previewBitmap)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(previewBitmap)
                    .showBigPictureWhenCollapsed(true)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(copyPendingIntent)
            .addAction(R.drawable.ic_close, getString(R.string.action_delete), deletePendingIntent)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }

            val captureChannel = NotificationChannel(
                CAPTURE_CHANNEL_ID,
                getString(R.string.screenshot_captured),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.screenshot_captured_desc)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(captureChannel)
        }
    }

    private fun cleanupProjection() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        isRunning = false
        if (activeService == this) {
            activeService = null
        }
        serviceScope.cancel()

        cleanupProjection()

        imageHandlerThread?.quitSafely()
        imageHandlerThread = null

        super.onDestroy()
    }
}
