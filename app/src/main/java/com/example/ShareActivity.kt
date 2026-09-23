package com.example

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import java.io.File

class ShareActivity : Activity() {

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
        const val EXTRA_URI_STRING = "extra_uri_string"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriString = intent.getStringExtra(EXTRA_URI_STRING)
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)

        val targetUri: Uri? = when {
            !uriString.isNullOrEmpty() -> Uri.parse(uriString)
            !filePath.isNullOrEmpty() -> {
                val file = File(filePath)
                if (file.exists()) FileHelper.getFileUri(this, file) else null
            }
            else -> null
        }

        if (targetUri != null) {
            copyImageToClipboard(targetUri)
        }

        finish()
    }

    private fun copyImageToClipboard(uri: Uri) {
        try {
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
            Toast.makeText(this, "Lỗi sao chép vào bộ nhớ tạm: ${e.message}", Toast.LENGTH_SHORT).show()
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
