package com.solankizm.ola.data.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

/**
 * Finds the scooter on the phone's hotspot/Wi-Fi by scanning the local /24 for
 * an open MoveOS diagnostics port (TCP 50005). Read-only: it just checks which
 * address answers on that port; it opens no session and sends no command.
 */
object NetworkScanner {

    suspend fun discover(timeoutMs: Int = 300): String? = withContext(Dispatchers.IO) {
        val self = localIpv4() ?: return@withContext null
        val base = self.substringBeforeLast('.') + "."
        val selfLast = self.substringAfterLast('.').toIntOrNull()

        val hits = coroutineScope {
            (1..254).filter { it != selfLast }.map { i ->
                async { if (portOpen("$base$i", 50005, timeoutMs)) "$base$i" else null }
            }.awaitAll()
        }
        hits.filterNotNull().firstOrNull()
    }

    private fun portOpen(ip: String, port: Int, timeoutMs: Int): Boolean = try {
        Socket().use { s ->
            s.connect(InetSocketAddress(ip, port), timeoutMs)
            true
        }
    } catch (_: Exception) {
        false
    }

    /** First site-local IPv4 (192.168./10./172.16-31) on an up, non-loopback interface. */
    private fun localIpv4(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .map { it.hostAddress ?: "" }
                .firstOrNull { ip ->
                    ip.count { it == '.' } == 3 &&
                        (ip.startsWith("192.168.") || ip.startsWith("10.") ||
                            Regex("^172\\.(1[6-9]|2\\d|3[01])\\.").containsMatchIn(ip))
                }
        } catch (_: Exception) {
            null
        }
    }
}
