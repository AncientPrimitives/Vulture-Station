package com.cobox.vulture.ddnsserver

/**
 * "ddns": {
 *   "vendor": {
 *     "name": "dnspod",
 *     "api_key": "227706",
 *     "api_token": "1a0b69860a6799ba718cc51eaa1ec8ea"
 *   },
 *   "report_interval_sec": 600
 * }
 */
object VultureConfig {

    object Key {
        const val DDNS = "ddns"

        const val VENDOR = "vendor"
        const val VENDOR_NAME = "name"
        const val REPORT_INTERVAL_SEC = "report_interval_sec"
        const val REPORT_DOMAIN = "report_domain"
    }

    object Default {
        const val REPORT_INTERVAL_SEC = 60 * 10L // 10min
        const val REPORT_DOMAIN = "vulture.com"
    }

}