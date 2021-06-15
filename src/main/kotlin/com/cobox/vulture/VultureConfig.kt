package com.cobox.vulture

/**
 * /config/http_gateway.config = {
 *   "http_listen_port": 8080,
 *   "http_host_domain": "vulture.com",
 *   "static_resource_root": "./data/static/",
 *   "address_reuse": false,
*    "port_reuse": true
 * }
 */
object VultureConfig {

    object Key {
        const val DEPLOYMENT = "deployment"
        const val VERTICLE = "verticle"
        const val ENABLE = "enable"
        const val INSTANCE = "instance"
        const val CONFIG = "config"
        const val COMMENT = "comment"

        const val HTTP_HOST_DOMAIN = "http_host_domain"
        const val HTTP_LISTEN_PORT = "http_listen_port"
        const val STATIC_RESOURCE_ROOT = "static_resource_root"
        const val ADDRESS_REUSE = "address_reuse"
        const val PORT_REUSE = "port_reuse"
    }

    object Default {
        const val VULTURE_CONFIG_FILE = "./config/vulture.config"
        const val CLUSTER_CONFIG_FILE = "./config/cluster.xml"

        const val ENABLE = false
        const val INSTANCE = 1
        const val CONFIG = ""

        const val HTTP_HOST_DOMAIN: String = ""
        const val HTTP_LISTEN_PORT = 8080
        const val STATIC_RESOURCE_ROOT = "./static/"
        const val ADDRESS_REUSE = false
        const val PORT_REUSE = true
    }

}

