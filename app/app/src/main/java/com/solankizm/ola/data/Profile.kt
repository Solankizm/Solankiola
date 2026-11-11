package com.solankizm.ola.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/**
 * The rider's display picture + name. Fully on-device. The photo is one file in
 * the app's private storage (no storage permission needed); the name lives in
 * SharedPreferences.
 *
 * Two ways to set the photo:
 *   1. In-app photo picker (Settings) — works on a phone, emulator, or a head
 *      unit that has a gallery. Uses the system picker, so no permission prompt.
 *   2. Pushed over ADB from the Mac with `app/set-photo.sh <ip> <photo.jpg>`,
 *      which copies it straight into files/profile.jpg via run-as (debug build).
 */
data class Profile(
    val name: String = "Rider",
    val photo: ImageBitmap? = null,
)

class ProfileStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("solankizm", Context.MODE_PRIVATE)
    private val photoFile = File(context.filesDir, "profile.jpg")

    fun load(): Profile =
        Profile(name = prefs.getString("name", "Rider") ?: "Rider", photo = readPhoto())

    fun saveName(name: String) {
        prefs.edit().putString("name", name.ifBlank { "Rider" }).apply()
    }

    /** Copy a picked image into private storage; returns the new photo bitmap. */
    fun savePhoto(uri: Uri): ImageBitmap? {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                photoFile.outputStream().use { input.copyTo(it) }
            }
        }
        return readPhoto()
    }

    private fun readPhoto(): ImageBitmap? {
        if (!photoFile.exists()) return null
        return runCatching {
            BitmapFactory.decodeFile(photoFile.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }
}
