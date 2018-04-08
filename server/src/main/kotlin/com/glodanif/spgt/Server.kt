package com.glodanif.spgt

import java.awt.Toolkit
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

private val mouseController = MouseController()

fun main(params: Array<String>) {

    val ip = getIpAddress()
    val port = if (params.isNotEmpty()) params[0].toInt() else 8003
    System.out.println("Port: $port, Local IP: $ip")

    WifiConnectionThread(port).apply {
        onEvent = { mouseController.onMouseEvent(it) }
        onConnected = { write(getScreenResolution()) }
    }.start()
}

fun getIpAddress(): String? {

    try {
        NetworkInterface.getNetworkInterfaces().toList().forEach {
            it.inetAddresses.toList()
                    .filter { !it.isLoopbackAddress && it is Inet4Address }
                    .forEach {
                        return it.hostAddress.toString()
                    }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }

    return null
}

fun getScreenResolution(): String {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val width = screenSize.getWidth().toInt()
    val height = screenSize.getHeight().toInt()
    return "$width:$height"
}