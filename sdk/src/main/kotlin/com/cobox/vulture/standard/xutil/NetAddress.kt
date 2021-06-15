package com.cobox.vulture.standard.xutil

import java.net.InetAddress

object NetAddress {

    val EMPTY_ADDRESS = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))

}