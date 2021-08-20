package com.cobox.vulture

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureHttpGateway
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.impl.future.FailedFuture
import io.vertx.core.impl.future.SucceededFuture
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.io.File

class HttpServerHost(
    private val vertx: Vertx,
    private val config: JsonObject
) {

    private var httpHostDomain: String? = VultureConfig.Default.HTTP_HOST_DOMAIN
    private var httpListenPort = VultureConfig.Default.HTTP_LISTEN_PORT
    private var httpAddressReuse = VultureConfig.Default.ADDRESS_REUSE
    private var httpPortReuse = VultureConfig.Default.PORT_REUSE
    private var staticResourceRoot = VultureConfig.Default.STATIC_RESOURCE_ROOT

    private var httpServer: HttpServer? = null
    private val httpRouter = Router.router(vertx)
    private val httpGateways = mutableMapOf<String, VultureHttpGateway>()

    fun prepare() {
        httpHostDomain = config.getString(VultureConfig.Key.HTTP_HOST_DOMAIN, VultureConfig.Default.HTTP_HOST_DOMAIN)
        httpListenPort = config.getInteger(VultureConfig.Key.HTTP_LISTEN_PORT, VultureConfig.Default.HTTP_LISTEN_PORT)
        httpAddressReuse = config.getBoolean(VultureConfig.Key.ADDRESS_REUSE, VultureConfig.Default.ADDRESS_REUSE)
        httpPortReuse = config.getBoolean(VultureConfig.Key.PORT_REUSE, VultureConfig.Default.PORT_REUSE)
        staticResourceRoot = config.getString(VultureConfig.Key.STATIC_RESOURCE_ROOT, VultureConfig.Default.STATIC_RESOURCE_ROOT)
    }

    fun launchHttpServer(handler: Handler<AsyncResult<Boolean>>? = null) {
        httpServer = httpServer ?: let {
            vertx.createHttpServer(
                HttpServerOptions().apply {
                    if (!httpHostDomain.isNullOrEmpty()) this.host = httpHostDomain
                    this.port = httpListenPort
                    this.isReuseAddress = httpAddressReuse
                    this.isReusePort = httpPortReuse
                }
            ).let { server ->
                createFaviconHandler(httpRouter)
                server.requestHandler(httpRouter)
                server.listen { result ->
                    if (result.succeeded()) {
                        Log.info(TAG, "[launchHttpServer] Http gateway server launched, listening at port ${result.result().actualPort()}")
                        handler?.handle(SucceededFuture(true))
                    } else {
                        Log.error(TAG, "[launchHttpServer] Http gateway server launched fail, caused by ${result.cause()}", result.cause())
                        handler?.handle(FailedFuture(result.cause()))
                    }
                }

                server
            }
        }
    }

    fun shutdownHttpServer(handler: Handler<AsyncResult<Boolean>>? = null) {
        httpServer?.let { server ->
            httpServer = null

            server.close { result ->
                if (result.succeeded()) {
                    Log.info(TAG, "[shutdownHttpServer] Http gateway server is shutdown")
                    handler?.handle(SucceededFuture(true))
                } else {
                    Log.error(TAG, "[shutdownHttpServer] Shut down http gateway server fail, caused by ${result.cause()}", result.cause())
                    handler?.handle(FailedFuture(result.cause()))
                }
            }
        }
    }

    fun registerSubGateway(gatewayName: String, gateway: VultureHttpGateway): Boolean {
        if (httpGateways.containsKey(gatewayName)) {
            Log.warn(TAG, "[registerSubRouter] Sub router '$gatewayName' has already registered to gateway, ignore")
            return false
        }

        httpRouter.route().let { newRoute ->
            gateway.name = gatewayName
            gateway.mount(newRoute)
            httpGateways[gatewayName] = gateway
        }
        return true
    }

    fun unregisterSubGateway(gatewayName: String): Boolean {
       return httpGateways[gatewayName]?.let { gateway ->
            httpRouter.routes.remove(gateway.routePoint)
            gateway.unmount()
            true
        } ?: let {
            Log.warn(TAG, "[unregisterSubRouter] Sub router '$gatewayName' isn't found in gateway, ignore")
            false
        }
    }

    private fun createFaviconHandler(router: Router) {
        val routeFile :((String) -> Unit) = { file ->
            router.route(file).blockingHandler { ctx ->
                val favicon = File(staticResourceRoot).canonicalPath + file
                vertx.fileSystem().readFile(favicon) {
                    if (it.succeeded()) ctx.end(it.result()) else ctx.fail(403)
                }
            }
        }

        routeFile("/favicon.ico")
        routeFile("/require.js")
        routeFile("/SystemMonitorLoader.js")
        routeFile("/SystemMonitorPage.js")
        routeFile("/SystemMonitor.js")
        routeFile("/Animation.js")
        routeFile("/HUD/HUD.js")
        routeFile("/HUD/CircleMeter.js")
        routeFile("/HUD/CurveMeter.js")
        routeFile("/fonts/GemunuLibre-SemiBold.ttf")
        routeFile("/fonts/DottedSongtiSquareRegular.otf")
    }
}