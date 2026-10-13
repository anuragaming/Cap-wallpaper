package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlin.math.abs

class FloatingShieldService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingContainer: FrameLayout? = null
    private var floatingBubble: FrameLayout? = null
    private var expandedPanel: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var isShieldActive = true

    companion object {
        const val ACTION_START = "com.example.service.START_FLOATING"
        const val ACTION_STOP = "com.example.service.STOP_FLOATING"
        const val NOTIFICATION_CHANNEL_ID = "anti_return_floating_channel"
        const val NOTIFICATION_ID = 1001

        var isServiceRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        isServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
                if (floatingContainer == null) {
                    setupFloatingView()
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Nhật Anh browser Floating Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị nút nổi Nhật Anh browser trên màn hình"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingShieldService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Nhật Anh browser - Nút Nổi Đang Bật")
            .setContentText("Chạm vào nút nổi để kiểm soát bảo vệ trên bất kỳ ứng dụng nào")
            .setSmallIcon(R.drawable.ic_shield_guard)
            .setContentIntent(pendingIntent)
            .addAction(0, "Tắt Nút Nổi", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        val wm = windowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        // Root container
        floatingContainer = FrameLayout(this)

        // 1. Floating Bubble (Default compact state)
        floatingBubble = createBubbleView()
        floatingContainer?.addView(floatingBubble)

        // 2. Expanded Panel (Hidden initially)
        expandedPanel = createExpandedPanelView()
        expandedPanel?.visibility = View.GONE
        floatingContainer?.addView(expandedPanel)

        // Dragging and Click Handling
        setupTouchListener()

        try {
            wm.addView(floatingContainer, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createBubbleView(): FrameLayout {
        val bubble = FrameLayout(this)
        val sizeDp = 58
        val sizePx = (sizeDp * resources.displayMetrics.density).toInt()
        val layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        bubble.layoutParams = layoutParams

        // Circular background with Cyber Cyan border & Dark Navy fill
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#0F172A"))
            setStroke((2.5f * resources.displayMetrics.density).toInt(), Color.parseColor("#00F0FF"))
        }
        bubble.background = bgDrawable

        // Gemini AI Sparkle Icon
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.ic_gemini_sparkle)
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        bubble.addView(icon)

        // Glowing Active Indicator Dot
        val dotSize = (12 * resources.displayMetrics.density).toInt()
        val dot = View(this).apply {
            val dotParams = FrameLayout.LayoutParams(dotSize, dotSize).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, (4 * resources.displayMetrics.density).toInt(), (4 * resources.displayMetrics.density).toInt())
            }
            this.layoutParams = dotParams
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#10B981"))
                setStroke((1.5f * resources.displayMetrics.density).toInt(), Color.parseColor("#0F172A"))
            }
        }
        bubble.addView(dot)

        return bubble
    }

    private fun createExpandedPanelView(): LinearLayout {
        val density = resources.displayMetrics.density
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 18 * density
                setColor(Color.parseColor("#111C30"))
                setStroke((1.5f * density).toInt(), Color.parseColor("#00F0FF"))
            }
            background = bg
        }

        // Header
        val header = TextView(this).apply {
            text = "✨ NHẬT ANH GEMINI & SHIELD"
            setTextColor(Color.parseColor("#00F0FF"))
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        }
        panel.addView(header)

        val statusText = TextView(this).apply {
            text = "Trạng Thái: ĐANG BẢO VỆ + GEMINI SẴN SÀNG"
            setTextColor(Color.parseColor("#10B981"))
            textSize = 12f
            setPadding(0, (6 * density).toInt(), 0, (10 * density).toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }
        panel.addView(statusText)

        // Button 1: Screenshot & Instant Solve in Browser (No Exit)
        val lensBtn = TextView(this).apply {
            text = "📸 Chụp & Giải Ngay Trong Web (Lens AI)"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val btnPad = (10 * density).toInt()
            setPadding(btnPad, btnPad, btnPad, btnPad)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10 * density
                setColor(Color.parseColor("#059669"))
                setStroke((1.5f * density).toInt(), Color.parseColor("#10B981"))
            }
            setOnClickListener {
                val intent = Intent(this@FloatingShieldService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("extra_trigger_lens_capture", true)
                }
                startActivity(intent)
                toggleExpanded(false)
            }
        }
        panel.addView(lensBtn)

        // Button 2: Gemini Homework Solver / Assistant in Browser
        val geminiBtn = TextView(this).apply {
            text = "✨ Trợ Lý Gemini - Giải Đề & Tìm Kiếm"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val btnPad = (9 * density).toInt()
            setPadding(btnPad, btnPad, btnPad, btnPad)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (6 * density).toInt(), 0, 0)
            }
            layoutParams = lp
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10 * density
                setColor(Color.parseColor("#0284C7"))
                setStroke((1.5f * density).toInt(), Color.parseColor("#00F0FF"))
            }
            setOnClickListener {
                val intent = Intent(this@FloatingShieldService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("extra_open_gemini", true)
                }
                startActivity(intent)
                toggleExpanded(false)
            }
        }
        panel.addView(geminiBtn)

        // Button: Open Anti Return Browser
        val openBrowserBtn = TextView(this).apply {
            text = "🌐 Mở Trình Duyệt Nhật Anh"
            setTextColor(Color.parseColor("#00F0FF"))
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val btnPad = (9 * density).toInt()
            setPadding(btnPad, btnPad, btnPad, btnPad)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (8 * density).toInt(), 0, 0)
            }
            layoutParams = lp
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10 * density
                setColor(Color.parseColor("#1E293B"))
                setStroke((1 * density).toInt(), Color.parseColor("#334155"))
            }
            setOnClickListener {
                val intent = Intent(this@FloatingShieldService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                toggleExpanded(false)
            }
        }
        panel.addView(openBrowserBtn)

        // Button: Close Panel
        val closeBtn = TextView(this).apply {
            text = "Thu Nhỏ Nút Nổi"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 11f
            gravity = Gravity.CENTER
            val btnPad = (8 * density).toInt()
            setPadding(btnPad, (8 * density).toInt(), btnPad, (2 * density).toInt())
            setOnClickListener {
                toggleExpanded(false)
            }
        }
        panel.addView(closeBtn)

        return panel
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMove = false

        floatingBubble?.setOnTouchListener { _, event ->
            val p = params ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMove = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isMove = true
                        p.x = initialX + dx
                        p.y = initialY + dy
                        windowManager?.updateViewLayout(floatingContainer, p)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMove) {
                        // User clicked the bubble
                        toggleExpanded(!isExpanded)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleExpanded(expand: Boolean) {
        isExpanded = expand
        if (expand) {
            floatingBubble?.visibility = View.GONE
            expandedPanel?.visibility = View.VISIBLE
        } else {
            expandedPanel?.visibility = View.GONE
            floatingBubble?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        floatingContainer?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
