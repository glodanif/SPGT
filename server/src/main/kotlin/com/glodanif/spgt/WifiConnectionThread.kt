package com.glodanif.spgt

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class WifiConnectionThread(port: Int) : Thread() {

    private val serverSocket = ServerSocket(port)
    private var socket: Socket? = null
    private var output: OutputStream? = null

    var onEvent: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    @Volatile
    private var shouldRun = true

    override fun run() {

        while (shouldRun) {

            println("Waiting...")

            socket = serverSocket.accept()
            println("Accepted connection : $socket")

            output = socket?.getOutputStream()
            onConnected?.invoke()

            val bytes = ByteArray(2048)
            val stream = socket?.getInputStream()

            do {
                val text = stream?.read(bytes)?.let {
                    String(bytes, 0, it)
                }

                text?.let {
                    onEvent?.invoke(it.trim())
                }

            } while (true)
        }
    }

    fun write(message: String) {
        output?.write(message.toByteArray())
        output?.flush()
    }

    fun cancel() {
        socket?.close()
        serverSocket.close()
        shouldRun = false
    }
}
