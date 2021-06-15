package com.cobox.vulture

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureGateway.Companion.REQUEST_FAILED
import com.cobox.vulture.busniess.framework.VultureHttpGateway
import com.cobox.vulture.busniess.framework.VultureServer
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.*

class HttpGatewayGroup: VultureServer() {

    companion object {
        const val TAG = "HttpGatewayGroup"
    }

    private val httpServer by lazy { HttpServerHost(vertx, config()) }
    private val httpRouters = mutableMapOf<String, Router>()

    override fun onServicePrepare() {
        httpServer.prepare()
    }

    override fun onRegisterService() {
        eventBus.consumer<String>("/http_gateway/register_gateway") { msg ->
            vertx.executeBlocking<String> {
                registerGateway(JsonObject(msg.body()), it)
            }.onSuccess { gatewayName ->
                msg.reply(
                    JsonObject()
                        .put("gatewayName", gatewayName)
                        .toString()
                )
            }.onFailure {
                msg.fail(REQUEST_FAILED, it.message)
                it.printStackTrace()
            }
        }
        eventBus.consumer<String>("/http_gateway/unregister_gateway") { msg ->
            vertx.executeBlocking<String> {
                unregisterGateway(JsonObject(msg.body()), it)
            }.onSuccess { gatewayName ->
                msg.reply(
                    JsonObject()
                        .put("gatewayName", gatewayName)
                        .toString()
                )
            }.onFailure {
                msg.fail(REQUEST_FAILED, it.message)
                it.printStackTrace()
            }
        }
    }

    override fun onServiceStart() {
        httpServer.launchHttpServer()
    }

    override fun onServiceStop() {
        httpServer.shutdownHttpServer()
    }

    override fun onUnregisterService() {
        eventBus.consumer<String>("/http_gateway/register_gateway", null)
        eventBus.consumer<String>("/http_gateway/unregister_gateway", null)
    }

    override fun onServiceDestroy() {

    }

    private fun registerGateway(config: JsonObject, promise: Promise<String>? = null) {
        Log.info(TAG, "[registerGateway] config: ${config}")

        Class.forName(config.getString("gatewayDeclaration"))?.let { gatewayDeclaration ->
            val gatewayName = "${gatewayDeclaration.name}(${UUID.randomUUID()})"
            val gateway = gatewayDeclaration.getDeclaredConstructor(
                Vertx::class.java,
                JsonObject::class.java
            ).newInstance(
                vertx,
                config.getJsonObject("config")
            ) as VultureHttpGateway

            if (httpServer.registerSubGateway(gatewayName, gateway)) {
                Log.info(TAG, "[registerGateway] Gateway '$gatewayName' is registered")
                promise?.complete(gatewayName)
            } else {
                Log.error(TAG, "[registerGateway] Gateway '$gatewayName' register fail, it seems already registered")
                promise?.fail("gateway exists")
            }
        } ?: let {
            promise?.fail("invalid gateway declaration")
        }
    }

    private fun unregisterGateway(config: JsonObject, promise: Promise<String>? = null) {
        Log.info(TAG, "[unregisterGateway] config: ${config}")

        val gatewayName = config.getString("gatewayName")
        if (httpServer.unregisterSubGateway(gatewayName)) {
            Log.info(TAG, "[unregisterGateway] Gateway '$gatewayName' is unregistered")
            promise?.complete(gatewayName)
        } else {
            Log.error(TAG, "[unregisterGateway] Gateway '$gatewayName' is unregistered")
            promise?.fail("gateway no found")
        }
    }

}