package com.cobox.utilites.system

object OS {

    val isWindows: Boolean
        get() = System.getProperty("os.name").toLowerCase().startsWith("windows")

    val isLinux: Boolean
        get() = System.getProperty("os.name").toLowerCase().startsWith("linux")

    val isMacos: Boolean
        get() = System.getProperty("os.name").toLowerCase().startsWith("mac")

}