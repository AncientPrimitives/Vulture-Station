package com.cobox.vulture.systemserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureHttpGateway
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router

class SystemGateway(
    vertx: Vertx,
    config: JsonObject?
): VultureHttpGateway(vertx, config) {

    companion object {
        const val TAG = "SystemGateway"
    }

    override fun onFillRoutes(): List<(Router) -> Unit> = listOf(
        systemCpuInfo,
        systemMemInfo,
        systemTempInfo,
        systemSummaryInfo,
        defaultRoute
    )

    override fun onConfigRoute(route: Route) {
        config
            .getJsonObject(VultureConfig.Key.GATEWAY)
            ?.getString(VultureConfig.Key.VIRTUAL_HOST, VultureConfig.Default.VIRTUAL_HOST)
            ?.let { virtualHost ->
                route.virtualHost(virtualHost)
            }
    }

    private val systemCpuInfo: (Router) -> Unit = { router ->
        router.get("/system/cpuinfo").blockingHandler { ctx ->
            val request = ctx.request()
            eventBus.request<String>("/system/cpuinfo", "") { result ->
                if (result.failed()) {
                    Log.error(TAG, "[systemCpuInfo] no server reply for '/system/cpuinfo'")
                    request.response().let { response ->
                        response.statusCode = 500
                        response.end("System server isn't deploy")
                    }
                    return@request
                }

                request.response().end(result.result().body())
            }
        }
    }

    private val systemMemInfo: (Router) -> Unit = { router ->
        router.get("/system/meminfo").blockingHandler { ctx ->
            val request = ctx.request()
            eventBus.request<String>("/system/meminfo", "") { result ->
                if (result.failed()) {
                    Log.error(TAG, "[systemMemInfo] no server reply for '/system/meminfo'")
                    request.response().let { response ->
                        response.statusCode = 500
                        response.end("System server isn't deploy")
                    }
                    return@request
                }

                request.response().end(result.result().body())
            }
        }
    }

    private val systemTempInfo: (Router) -> Unit = { router ->
        router.get("/system/tempinfo").blockingHandler { ctx ->
            val request = ctx.request()
            eventBus.request<String>("/system/tempinfo", "") { result ->
                if (result.failed()) {
                    Log.error(TAG, "[systemMemInfo] no server reply for '/system/tempinfo'")
                    request.response().let { response ->
                        response.statusCode = 500
                        response.end("System server isn't deploy")
                    }
                    return@request
                }

                request.response().end(result.result().body())
            }
        }
    }

    private val systemSummaryInfo: (Router) -> Unit = { router ->
        router.get("/system/summary").blockingHandler { ctx ->
            val request = ctx.request()
            eventBus.request<String>("/system/summary", "") { result ->
                if (result.failed()) {
                    Log.error(TAG, "[systemMemInfo] no server reply for '/system/summary'")
                    request.response().let { response ->
                        response.statusCode = 500
                        response.end("System server isn't deploy")
                    }
                    return@request
                }

                request.response().end(result.result().body())
            }
        }
    }

    private val defaultRoute: (Router) -> Unit = { router ->
        router.errorHandler(500) {
            it.end("No vulture service found for ${it.request().absoluteURI()}")
        }

        router.route().handler { ctx ->
            ctx.next()
        }
    }

}