package com.cobox.vulture.nasserver

/**
 * {
 *   "gateway": {
 *     "virtual_host": "api.vulture.com",
 *   },
 *   "server": {
 *     "nas_repo": "./data/storage/nas/"
 *   }
 * }
 */
object VultureConfig {

    object Key {
        const val GATEWAY = "gateway"
        const val DOMAIN = "domain"

        const val VIRTUAL_HOST = "virtual_host"
        const val NAS_REPO = "nas_repo"
    }

    object Default {
        const val VIRTUAL_HOST = "api.vulture.com"
        const val NAS_REPO = "./data/storage/nas/" // "/storage/vulture/nas/"
    }

}