package com.cobox.vulture.ddnsserver

import com.cobox.vulture.busniess.framework.VultureServer
import io.vertx.core.json.JsonObject

class DdnsServer: VultureServer() {

    private val ddnsReporter by lazy {
        DdnsReporter(
            vertx,
            eventBus,
            config().getJsonObject(VultureConfig.Key.DDNS)
        )
    }

    companion object {
        const val TAG = "DdnsServer"
    }

    override fun onServicePrepare() {
        ddnsReporter.prepare()
    }

    override fun onRegisterService() {
        eventBus.consumer<String>("/ddns/public_address") { msg ->
            ddnsReporter.queryPublicAddress { result ->
                if (result.succeeded()) {
                    msg.reply(
                        JsonObject()
                            .put("public_address", result.result().hostAddress)
                            .toString()
                    )
                } else {
                    msg.fail(404, "Network broken")
                }
            }
        }
        eventBus.consumer<String>("/ddns/report_immediately") { msg ->
            ddnsReporter.refresh { result ->
                msg.reply(
                    JsonObject()
                        .put("result", "OK")
                        .toString()
                )
            }
        }
    }

    override fun onServiceStart() {
        ddnsReporter.launch()
    }

    override fun onServiceStop() {
        ddnsReporter.shutdown()
    }

    override fun onUnregisterService() {

    }

    override fun onServiceDestroy() {
        ddnsReporter.destroy()
    }

}