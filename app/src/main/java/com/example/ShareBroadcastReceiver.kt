package com.example

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

class ShareBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DELETE = "com.example.snapshare.ACTION_DELETE"
        const val ACTION_STOP_SERVICE = "com.example.snapshare.ACTION_STOP_SERVICE"
        const val ACTION_SHARE_CALLBACK = "com.example.snapshare.ACTION_SHARE_CALLBACK"

        const val EXTRA_FILE_PATH = "extra_file_path"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            ACTION_DELETE -> {
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                FileHelper.deleteFilePath(context, filePath)

                if (notificationId != -1) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            }

            ACTION_STOP_SERVICE -> {
                val serviceIntent = Intent(context, ScreenCaptureService::class.java).apply {
                    action = ScreenCaptureService.ACTION_STOP
                }
                context.startService(serviceIntent)
            }

            ACTION_SHARE_CALLBACK -> {
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                // Delete the temporary file after a short delay so the receiving app finishes reading it
                Handler(Looper.getMainLooper()).postDelayed({
                    FileHelper.deleteFilePath(context, filePath)
                }, 5000L)
            }
        }
    }
}
