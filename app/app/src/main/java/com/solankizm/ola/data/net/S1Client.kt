package com.solankizm.ola.data.net

import android.util.Base64
import io.grpc.CallOptions
import io.grpc.ClientCall
import io.grpc.ManagedChannel
import io.grpc.MethodDescriptor
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Read-only gRPC client for the MoveOS root diagnostics daemon (TCP 50005).
 * Ported from the reference S1Mods client, with every write/root path removed:
 * no ADB unlock, no setprop, no push, no mount. It only runs read commands and
 * reads their output. The daemon runs unauthenticated, so this needs no ADB
 * pairing — but only ever point it at YOUR scooter on YOUR hotspot.
 */
class S1Client(
    val host: String,
    val port: Int = 50005,
    val scope: Int? = null,
) {
    private var channel: ManagedChannel? = null

    private fun descriptor(method: String): MethodDescriptor<ByteArray, ByteArray> =
        MethodDescriptor.newBuilder<ByteArray, ByteArray>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(MethodDescriptor.generateFullMethodName("wifican.WifiCanRpc", method))
            .setRequestMarshaller(ByteArrayMarshaller)
            .setResponseMarshaller(ByteArrayMarshaller)
            .build()

    private val execMethod = descriptor("ExecuteADBCommand")

    private object ByteArrayMarshaller : MethodDescriptor.Marshaller<ByteArray> {
        override fun stream(value: ByteArray): InputStream = ByteArrayInputStream(value)
        override fun parse(stream: InputStream): ByteArray = stream.readBytes()
    }

    private fun resolveTargetHost(): String {
        var h = host.trim()
        if (h.startsWith("ipv6:", ignoreCase = true)) h = h.substring(5).trim()
        h = h.trim('[', ']')
        if (h.startsWith("fe80", ignoreCase = true) && !h.contains("%") && scope != null) {
            h = "$h%$scope"
        }
        return try {
            InetAddress.getByName(h).hostAddress ?: h
        } catch (_: Exception) {
            h
        }
    }

    suspend fun connect(timeoutSeconds: Long = 8L) = withContext(Dispatchers.IO) {
        val target = resolveTargetHost()
        channel?.shutdownNow()
        channel = OkHttpChannelBuilder.forAddress(target, port)
            .usePlaintext()
            .maxInboundMessageSize(16 * 1024 * 1024)
            .build()
        val (status, out) = executeRaw("id -u", timeoutSeconds)
        if (status !in 0..1 && out.isEmpty()) {
            throw IllegalStateException("No answer from scooter on port $port at $target")
        }
    }

    suspend fun executeRaw(command: String, timeoutSeconds: Long = 30L): Pair<Int, String> =
        unary(execMethod, ProtoUtils.buildCommandPayload(command), timeoutSeconds)

    suspend fun getSwVersion(timeoutSeconds: Long = 15L): String =
        unary(descriptor("GetSwVersion"), ByteArray(0), timeoutSeconds).second

    /** Runs a read command through sh so quotes/pipes survive the whitespace split. */
    suspend fun shell(command: String, timeoutSeconds: Long = 30L): Pair<Int, String> {
        val b64 = Base64.encodeToString(command.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        val wrapped = "sh -c echo\${IFS}$b64|base64\${IFS}-d|sh"
        return executeRaw(wrapped, timeoutSeconds)
    }

    private suspend fun unary(
        method: MethodDescriptor<ByteArray, ByteArray>,
        payload: ByteArray,
        timeoutSeconds: Long,
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        val ch = channel ?: throw IllegalStateException("Not connected")
        val call: ClientCall<ByteArray, ByteArray> =
            ch.newCall(method, CallOptions.DEFAULT.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS))

        val holder = CompletableResult<ByteArray>()
        call.start(object : ClientCall.Listener<ByteArray>() {
            override fun onMessage(message: ByteArray) = holder.complete(message)
            override fun onClose(status: io.grpc.Status, trailers: io.grpc.Metadata) {
                if (!status.isOk && !holder.isDone()) holder.completeExceptionally(status.asRuntimeException())
            }
        }, io.grpc.Metadata())

        call.request(1)
        call.sendMessage(payload)
        call.halfClose()

        ProtoUtils.decodeResponse(holder.await())
    }

    /**
     * Pushes bytes to a file on the scooter via the root daemon (base64 chunks).
     * Used to sideload an APK; no ADB key unlock or mount-bind needed.
     */
    suspend fun push(data: ByteArray, remotePath: String, onProgress: (Float) -> Unit = {}): Boolean {
        val encoded = Base64.encodeToString(data, Base64.NO_WRAP)
        val chunk = 50_000
        val total = (encoded.length + chunk - 1) / chunk
        var idx = 0
        var start = 0
        while (start < encoded.length) {
            val end = (start + chunk).coerceAtMost(encoded.length)
            val part = encoded.substring(start, end)
            val redirect = if (start == 0) ">" else ">>"
            val cmd = "sh -c echo\${IFS}$part|base64\${IFS}-d$redirect$remotePath"
            val (status, msg) = executeRaw(cmd, 60L)
            if (status !in 0..1) throw IllegalStateException("Push failed at byte $start: $msg")
            idx++
            onProgress(idx.toFloat() / total.toFloat())
            start = end
        }
        return true
    }

    fun close() {
        try { channel?.shutdownNow() } catch (_: Exception) {}
        channel = null
    }

    private class CompletableResult<T> {
        private val lock = Object()
        private var result: T? = null
        private var error: Throwable? = null
        private var done = false

        fun complete(value: T) = synchronized(lock) { result = value; done = true; lock.notifyAll() }
        fun completeExceptionally(e: Throwable) = synchronized(lock) { error = e; done = true; lock.notifyAll() }
        fun isDone(): Boolean = synchronized(lock) { done }
        fun await(): T = synchronized(lock) {
            while (!done) lock.wait()
            error?.let { throw it }
            result ?: throw IllegalStateException("null result")
        }
    }
}
