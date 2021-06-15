package com.cobox.vulture.database

import com.cobox.vulture.database.DatabaseStore
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class DataRepository : AbstractVerticle() {

    companion object {
        const val TAG = "DataRepository"
    }

    private val database by lazy {
        DatabaseStore(vertx, config())
    }

    private val userAgent: Any = Any()

    private val nasAgent: Any = Any()

    override fun start(startPromise: Promise<Void>?) {
        vertx.executeBlocking<Void> { task ->
            database.start {
                task.complete()
            }
        }.onComplete {
            startPromise?.complete()
        }
    }

    override fun stop(stopPromise: Promise<Void>?) {
        vertx.executeBlocking<Void> { task ->
            database.stop {
                task.complete()
            }
        }.onComplete {
            stopPromise?.complete()
        }
    }

}