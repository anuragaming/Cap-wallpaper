package com.example

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object FileHelper {

    private const val SCREENSHOT_DIR = "screenshots"

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): File? {
        return try {
            val cacheDir = File(context.cacheDir, SCREENSHOT_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Cleanup older temporary screenshots (older than 15 minutes)
            cleanupOldScreenshots(cacheDir, maxAgeMillis = 15 * 60 * 1000)

            val file = File(cacheDir, "snap_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun deleteFile(file: File?): Boolean {
        return try {
            if (file != null && file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteFilePath(context: Context, filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        return try {
            val file = File(filePath)
            // Only allow deleting within the app's cache directory
            if (file.exists() && file.canonicalPath.startsWith(context.cacheDir.canonicalPath)) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun cleanupOldScreenshots(dir: File, maxAgeMillis: Long) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAgeMillis) {
                file.delete()
            }
        }
    }
}
