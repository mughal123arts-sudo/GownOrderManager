package com.mughalarts.gownordermanager.slip

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object SlipExporter {

    /** Saves the slip as JPEG in the app cache and returns the Android Share Sheet intent. */
    fun createShareIntent(context: Context, bitmap: Bitmap, baseName: String): Intent {
        val dir = File(context.cacheDir, "slips")
        dir.mkdirs()
        val file = File(dir, "$baseName.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )

        val send = Intent(Intent.ACTION_SEND)
        send.type = "image/jpeg"
        send.putExtra(Intent.EXTRA_STREAM, uri)
        send.putExtra(Intent.EXTRA_SUBJECT, "Agreement Slip")
        send.clipData = ClipData.newRawUri("Agreement Slip", uri)
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return Intent.createChooser(send, "Share Agreement Slip")
    }

    /** Saves the slip as JPEG into Pictures/GownOrderManager (Android 10 or newer). */
    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/GownOrderManager"
        )
        values.put(MediaStore.Images.Media.IS_PENDING, 1)

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false

        return try {
            val written = resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            } ?: false
            if (written) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                resolver.delete(uri, null, null)
                false
            }
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }
}
