package com.glodanif.spgt

import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.io.OutputStream
import java.net.Socket

class WiFiConnection(private val ipAddress: String, private val port: Int) : AutoCloseable {

    private var socket: Socket? = null
    private var stream: OutputStream? = null

    private val lock = Any()

    var onResolution: ((String) -> Unit)? = null

    @Volatile
    private var shouldRead = true

    fun prepare() = launch {
        socket = Socket(ipAddress, port)
        stream = socket?.getOutputStream()

        while (shouldRead) {

            val bytes = ByteArray(2048)
            val stream = socket?.getInputStream()

            do {
                val text = stream?.read(bytes)?.let {
                    String(bytes, 0, it)
                }

                if (text != null) {
                    onResolution?.invoke(text)
                    break
                }
            } while (true)
        }
    }

    fun write(message: String)/* = launch*/ {

        synchronized(lock) {
            stream?.write((message + System.getProperty("line.separator")).toByteArray(Charsets.UTF_8))
            stream?.flush()
        }
    }

    override fun close() {
        shouldRead = false
        try {
            socket?.close()
            stream?.close()
            socket = null
            stream = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
