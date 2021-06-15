package com.cobox.vulture.busniess.framework

import com.cobox.utilites.annotation.BlockThread
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.impl.future.FailedFuture
import io.vertx.core.impl.future.SucceededFuture
import io.vertx.core.json.JsonObject

/**
 * 每个服务节点应该独立、去耦。其生命周期应如下：
 * 1. 被部署：被Vulture根据配置创建并部署到集群中
 * 2. 自举：
 *    - 向网关注册路由
 *    - 向总线注册对外接口
 */
open class VultureServer : AbstractVerticle() {

    protected lateinit var eventBus: EventBus

    override fun start(startPromise: Promise<Void>?) {
        eventBus = vertx.eventBus()
        vertx.executeBlocking<Void> { task ->
            onServicePrepare()
            onRegisterService()
            onServiceStart()
            task.complete()
        }.onComplete {
            startPromise?.complete()
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun stop(stopPromise: Promise<Void>?) {
        vertx.executeBlocking<Void> { task ->
            onServiceStop()
            onUnregisterService()
            onServiceDestroy()
            task.complete()
        }.onComplete {
            stopPromise?.complete()
        }.onFailure {
            it.printStackTrace()
        }
    }

    /**
     * 初始化服务，准备服务所需的组件及初试数据
     */
    @BlockThread
    open protected fun onServicePrepare() {}

    /**
     * 注册对外服务项目
     */
    @BlockThread
    open protected fun onRegisterService() {}

    /**
     * 开始提供服务
     */
    @BlockThread
    open protected fun onServiceStart() {}

    /**
     * 停止提供服务
     */
    @BlockThread
    open protected fun onServiceStop() {}

    /**
     * 注销对外服务项目
     */
    @BlockThread
    open protected fun onUnregisterService() {}

    /**
     * 关闭服务，并释放服务的资源及组件
     */
    @BlockThread
    open protected fun onServiceDestroy() {}

    protected fun createHttpGateway(
        gatewayDeclaration: String,
        config: JsonObject? = null,
        handler: Handler<AsyncResult<String>>
    ) {
        eventBus.request<String>(
            "/http_gateway/register_gateway",
            JsonObject()
                .put("gatewayDeclaration", gatewayDeclaration)
                .put("config", config)
                .toString()
        ) { ack ->
            if (ack.succeeded()) {
                handler.handle(SucceededFuture(
                    JsonObject(ack.result().body()).getString("gatewayName")
                ))
            } else {
                handler.handle(FailedFuture(ack.cause()))
            }
        }
    }

    protected fun destroyHttpGateway(
        gatewayName: String,
        handler: Handler<AsyncResult<Boolean>>
    ) {
        eventBus.request<String>(
            "/http_gateway/unregister_gateway",
            JsonObject()
                .put("gatewayName", gatewayName)
                .toString()
        ) { ack ->
            if (ack.succeeded()) {
                handler.handle(SucceededFuture(true))
            } else {
                handler.handle(FailedFuture(ack.cause()))
            }
        }
    }

}