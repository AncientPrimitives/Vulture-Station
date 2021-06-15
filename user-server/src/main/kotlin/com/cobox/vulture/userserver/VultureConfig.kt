package com.cobox.vulture.userserver

/**
 * {
 *   "gateway": {
 *     "virtual_host": "api.vulture.com"
 *   },
 *   "server": {
 *
 *   }
 * }
 */
object VultureConfig {

    object Key {
        const val GATEWAY = "gateway"
        const val SERVER = "server"

        const val VIRTUAL_HOST = "virtual_host"
    }

    object Default {
        const val VIRTUAL_HOST = "api.vulture.com"
    }

}