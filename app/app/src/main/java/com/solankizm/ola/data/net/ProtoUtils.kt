package com.solankizm.ola.data.net

import java.nio.charset.StandardCharsets

/**
 * Hand-rolled protobuf for the weakly-typed MoveOS daemon (wifican.WifiCanRpc).
 * Request  = field 1 (length-delimited): the command string.
 * Response = field 1 (varint) status, field 2 (string) output.
 * Matches the wire format used by ola.py / the reference S1Mods client.
 */
object ProtoUtils {

    fun buildCommandPayload(command: String): ByteArray {
        val raw = command.toByteArray(StandardCharsets.UTF_8)
        val out = ArrayList<Byte>()
        out += varint((1 shl 3) or 2).toList()   // field 1, wire type 2
        out += varint(raw.size).toList()
        out += raw.toList()
        return out.toByteArray()
    }

    fun decodeResponse(buf: ByteArray): Pair<Int, String> {
        var i = 0
        var status = 0
        var message = ""
        while (i < buf.size) {
            val (key, ni) = readVarint(buf, i); i = ni
            val field = key ushr 3
            when (key and 7) {
                0 -> { val (v, nj) = readVarint(buf, i); i = nj; if (field == 1) status = v }
                2 -> {
                    val (len, nj) = readVarint(buf, i); i = nj
                    val end = (i + len).coerceAtMost(buf.size)
                    val s = String(buf, i, end - i, StandardCharsets.UTF_8)
                    i = end
                    if (field == 2) message = s
                }
                else -> break
            }
        }
        return status to message
    }

    private fun varint(value: Int): ByteArray {
        var n = value
        val out = ArrayList<Byte>()
        while (true) {
            val b = n and 0x7F
            n = n ushr 7
            if (n != 0) out.add((b or 0x80).toByte()) else { out.add(b.toByte()); break }
        }
        return out.toByteArray()
    }

    private fun readVarint(buf: ByteArray, start: Int): Pair<Int, Int> {
        var value = 0; var shift = 0; var i = start
        while (i < buf.size) {
            val b = buf[i].toInt() and 0xFF; i++
            value = value or ((b and 0x7F) shl shift)
            if (b and 0x80 == 0) break
            shift += 7
        }
        return value to i
    }
}
