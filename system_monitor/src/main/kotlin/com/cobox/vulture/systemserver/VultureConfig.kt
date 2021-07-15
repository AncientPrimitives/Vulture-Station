package com.cobox.vulture.systemserver

/**
 * {
 *   "gateway": {
 *     "virtual_host": "api.vulture.com"
 *   },
 *   "server": {
 *     "monitor_refresh_interval": 1000
 *   }
 * }
 */
object VultureConfig {

    object Key {
        const val GATEWAY = "gateway"
        const val VIRTUAL_HOST = "virtual_host"

        const val SERVER = "server"
        const val MONITOR_REFRESH_INTERVAL = "monitor_refresh_interval"
    }

    object Default {
        const val MONITOR_REFRESH_INTERVAL: Long = 1000L
        const val VIRTUAL_HOST = "api.vulture.com"
    }

}