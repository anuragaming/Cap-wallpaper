package com.example

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class ShareActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
    }

    private var filePath: String? = null

    private val shareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // When the user dismisses or finishes the Sharesheet
        scheduleFileCleanup()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (filePath.isNullOrEmpty()) {
            finish()
            return
        }

        val file = File(filePath!!)
        if (!file.exists()) {
            finish()
            return
        }

        try {
            val uri: Uri = FileHelper.getFileUri(this, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val callbackIntent = Intent(this, ShareBroadcastReceiver::class.java).apply {
                action = ShareBroadcastReceiver.ACTION_SHARE_CALLBACK
                putExtra(ShareBroadcastReceiver.EXTRA_FILE_PATH, filePath)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                (System.currentTimeMillis() % 10000).toInt(),
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val chooser = Intent.createChooser(
                shareIntent,
                getString(R.string.share_screenshot_title),
                pendingIntent.intentSender
            )

            shareLauncher.launch(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun scheduleFileCleanup() {
        val path = filePath ?: return
        // Give 5 seconds for receiving apps (e.g. Messaging, Drive) to finish reading stream
        Handler(Looper.getMainLooper()).postDelayed({
            FileHelper.deleteFilePath(applicationContext, path)
        }, 5000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduleFileCleanup()
    }
}
