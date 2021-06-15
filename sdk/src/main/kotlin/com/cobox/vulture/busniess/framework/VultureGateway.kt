package com.cobox.vulture.busniess.framework

import io.vertx.core.Vertx

/**
 * 服务从系统外部接入请求到内部的VultureServer
 */
open class VultureGateway(
    protected val vertx: Vertx
) {

    companion object {
        const val REQUEST_FAILED = -1
    }

    protected val eventBus = vertx.eventBus()

}