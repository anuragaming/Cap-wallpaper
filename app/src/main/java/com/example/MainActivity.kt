package com.example

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private val mediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var showPermissionCard by mutableStateOf(false)
    private var permissionMessage by mutableStateOf("")

    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        launchMediaProjectionPrompt()
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            showPermissionCard = false
            startServiceAndCapture(result.resultCode, result.data!!)
        } else {
            permissionMessage = "Cần cấp quyền chụp màn hình để SnapShare có thể chụp và copy ảnh ngay khi chạm vào app."
            showPermissionCard = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. If service is ALREADY active: Capture right now and finish immediately!
        if (ScreenCaptureService.activeService != null) {
            performInstantCaptureAndFinish()
            return
        }

        // 2. If service is not active: Prompt permissions immediately (no extra button clicks needed)
        startPermissionFlow()

        setContent {
            MyApplicationTheme {
                if (showPermissionCard) {
                    PermissionPromptScreen(
                        message = permissionMessage,
                        onGrantClick = { launchMediaProjectionPrompt() }
                    )
                }
            }
        }
    }

    private fun startPermissionFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            launchMediaProjectionPrompt()
        }
    }

    private fun launchMediaProjectionPrompt() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val config = MediaProjectionConfig.createConfigForDefaultDisplay()
                mediaProjectionManager.createScreenCaptureIntent(config)
            } else {
                mediaProjectionManager.createScreenCaptureIntent()
            }
            mediaProjectionLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            permissionMessage = "Lỗi khi yêu cầu quyền chụp: ${e.message}"
            showPermissionCard = true
        }
    }

    private fun startServiceAndCapture(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_START
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_DATA, data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        lifecycleScope.launch {
            // Wait for service to initialize VirtualDisplay
            var waited = 0
            while (ScreenCaptureService.activeService == null && waited < 1500) {
                delay(50)
                waited += 50
            }
            delay(100)
            performInstantCaptureAndFinish()
        }
    }

    private fun performInstantCaptureAndFinish() {
        lifecycleScope.launch {
            // Allow transient window transition to settle
            delay(80)

            val service = ScreenCaptureService.activeService
            val bitmap = service?.captureFrame()

            if (bitmap != null) {
                val file = withContext(Dispatchers.IO) {
                    FileHelper.saveBitmapToCache(applicationContext, bitmap)
                }

                if (file != null) {
                    copyFileToClipboard(file)
                    service.showScreenshotNotification(file, bitmap)
                } else {
                    Toast.makeText(applicationContext, "Lỗi khi lưu ảnh tạm", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Không thể chụp màn hình", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    private fun copyFileToClipboard(file: File) {
        try {
            val uri = FileHelper.getFileUri(this, file)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData(
                ClipDescription(getString(R.string.clipboard_label), arrayOf("image/png")),
                ClipData.Item(uri)
            )
            clipboard.setPrimaryClip(clipData)

            triggerHapticFeedback()
            Toast.makeText(
                this,
                getString(R.string.toast_copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi copy vào bộ nhớ tạm: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun PermissionPromptScreen(
    message: String = "",
    onGrantClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SnapShare - Chụp & Copy",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message.ifEmpty {
                        "Chạm vào icon ứng dụng để chụp và copy ảnh trực tiếp vào bàn phím."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onGrantClick
                ) {
                    Text("Bắt đầu cấp quyền")
                }
            }
        }
    }
}
