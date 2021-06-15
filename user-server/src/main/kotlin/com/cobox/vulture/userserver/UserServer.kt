package com.cobox.vulture.userserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureGateway.Companion.REQUEST_FAILED
import com.cobox.vulture.busniess.framework.VultureServer
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.impl.future.SucceededFuture
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.VertxContextPRNG

class UserServer : VultureServer() {

    companion object {
        const val TAG = "UserServer"
    }

    private var gatewayName: String? = null

    override fun onServicePrepare() {
        createHttpGateway(UserGateway::class.java.name, config()) { result ->
            if (result.succeeded()) {
                gatewayName = result.result()
            } else {
                Log.error(TAG, "[onServicePrepare] register gateway failed, caused by ${result.cause()}", result.cause())
            }
        }
    }

    override fun onRegisterService() {
        eventBus.consumer<String>("/user/auth/") { msg ->
            signToken(JsonObject(msg.body())) { result ->
                if (result.succeeded()) {
                    msg.reply(
                        JsonObject()
                            .put("token", result.result())
                            .toString()
                    )
                } else {
                    msg.fail(REQUEST_FAILED, result.cause().message)
                }
            }
        }
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

    private fun signToken(config: JsonObject, handler: Handler<AsyncResult<String>>) {
        val username = config.getString("username")
        val password = config.getString("password")
        VertxContextPRNG.current(vertx).nextString(16).let { token ->
            Log.info(TAG, "[Authentication] '${username}:${password}' authenticated as $token")
            handler.handle(SucceededFuture(token))
        }
    }

}