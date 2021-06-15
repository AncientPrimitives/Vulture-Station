package com.cobox.vulture.nasserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureServer
import io.vertx.core.json.JsonObject

class NasServer : VultureServer() {

    companion object {
        const val TAG = "NasServer"
    }

    private var gatewayName: String? = null
    private val model: NasModel by lazy { NasModel(vertx, config()) }

    override fun onServicePrepare() {
        createHttpGateway(NasGateway::class.java.name, config()) { result ->
            if (result.succeeded()) {
                gatewayName = result.result()
            } else {
                Log.error(TAG, "[onServicePrepare] register gateway failed, caused by ${result.cause()}", result.cause())
            }
        }
    }

    override fun onRegisterService() {
        eventBus.consumer<String>("/nas/usage") { msg ->
            val result = JsonObject()
                .put("usage", model.usage.usable)
                .put("total", model.usage.total)
                .toString()
            msg.reply(result)
            Log.info(TAG, "[consumer] /nas/usage: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/nas/usage' on event bus")

        eventBus.consumer<String>("/nas/storage_root") { msg ->
            val result = JsonObject()
                .put("root", model.repo)
                .toString()
            msg.reply(result)
            Log.info(TAG, "[consumer] /nas/storage_root: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/nas/storage_root' on event bus")
    }

    override fun onServiceStart() {

    }

    override fun onServiceStop() {

    }

    override fun onUnregisterService() {

    }

    override fun onServiceDestroy() {
        gatewayName?.let {
            destroyHttpGateway(it) { result ->
                if (result.failed()) {
                    Log.error(TAG, "[onServiceDestroy] unregister gateway failed, caused by ${result.cause()}", result.cause())
                }
            }
        }
    }

}