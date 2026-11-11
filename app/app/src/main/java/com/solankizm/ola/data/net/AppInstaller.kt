package com.solankizm.ola.data.net

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Installs an APK onto the head unit through the root diagnostics daemon only:
 *   download/copy -> push to /data/local/tmp -> `pm install` as root -> clean up.
 * No ADB key unlock, no mount-bind, no persistent setprop. These are third-party
 * binaries installed at the user's own risk (see the warning on the Apps screen).
 */
object AppInstaller {

    private const val REMOTE = "/data/local/tmp/sk_install.apk"

    suspend fun downloadToCache(
        ctx: Context,
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit = {},
    ): File = withContext(Dispatchers.IO) {
        val out = File(ctx.cacheDir, fileName)
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 30000; instanceFollowRedirects = true
        }
        conn.connect()
        if (conn.responseCode !in 200..299) {
            throw IllegalStateException("Download failed (HTTP ${conn.responseCode})")
        }
        val length = conn.contentLength.toLong()
        conn.inputStream.use { input ->
            out.outputStream().use { output ->
                val buf = ByteArray(64 * 1024)
                var read: Int
                var done = 0L
                while (input.read(buf).also { read = it } != -1) {
                    output.write(buf, 0, read)
                    done += read
                    if (length > 0) onProgress(0.5f * (done.toFloat() / length))
                }
            }
        }
        out
    }

    suspend fun copyUriToCache(ctx: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val out = File(ctx.cacheDir, "picked_${System.currentTimeMillis()}.apk")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { input.copyTo(it) }
        } ?: throw IllegalStateException("Could not read the selected file.")
        out
    }

    /** Push the APK over gRPC, install as root, grant overlay if needed, clean up. */
    suspend fun install(
        client: S1Client,
        apk: File,
        packageName: String,
        grantOverlay: Boolean,
        onProgress: (Float) -> Unit = {},
    ): String = withContext(Dispatchers.IO) {
        client.push(apk.readBytes(), REMOTE) { p -> onProgress(0.5f + 0.4f * p) }
        onProgress(0.92f)
        val (_, out) = client.shell("pm install -r -g $REMOTE", 180L)
        if (!out.contains("Success", ignoreCase = true)) {
            // Some builds reject the runtime-grant flag; retry plainly.
            val (_, out2) = client.shell("pm install -r $REMOTE", 180L)
            client.shell("rm -f $REMOTE", 20L)
            if (!out2.contains("Success", ignoreCase = true)) {
                throw IllegalStateException(out2.ifBlank { out }.take(140))
            }
        } else {
            client.shell("rm -f $REMOTE", 20L)
        }
        if (grantOverlay) {
            runCatching { client.shell("appops set $packageName SYSTEM_ALERT_WINDOW allow", 20L) }
            runCatching { client.shell("pm grant $packageName android.permission.SYSTEM_ALERT_WINDOW", 20L) }
        }
        onProgress(1f)
        "Installed"
    }

    suspend fun uninstall(client: S1Client, packageName: String): String =
        withContext(Dispatchers.IO) { client.shell("pm uninstall $packageName", 60L).second.trim() }

    suspend fun launch(client: S1Client, packageName: String): Boolean =
        withContext(Dispatchers.IO) {
            val out = client.shell("monkey -p $packageName -c android.intent.category.LAUNCHER 1", 20L).second
            !out.contains("No activities found") && !out.contains("aborted")
        }

    suspend fun installedPackages(client: S1Client): Set<String> = withContext(Dispatchers.IO) {
        client.shell("pm list packages", 20L).second
            .lineSequence()
            .map { it.trim().removePrefix("package:").trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
