package com.cobox.vulture.busniess.framework

import io.vertx.core.Vertx

open class VultureServerModel(
    protected val vertx: Vertx
) {

    fun prepareCache() {
        vertx.executeBlocking<Void> { task ->
            kotlin.runCatching {
                onPrepareCache()
            }
            task.complete()
        }
    }

    open protected fun onPrepareCache() {}

}