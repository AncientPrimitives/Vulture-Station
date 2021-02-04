package com.cobox.iot.vulture.webgate

import com.cobox.iot.vulture.application.Application
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GateWay(
    private val app: Application
) : AbstractVerticle(),
    AuthorityApi by AuthorityApiImpl(app) {

    private lateinit var router: Router
    private lateinit var httpServer: HttpServer

    override fun start() {
        super.start()
        println("[WEB_GATE] start WebGate server")

        // Bind "/" to our hello message - so we are still compatible.
        router = Router.router(vertx)
        registerRoute(router)

        // Create the HTTP server and pass the "accept" method to the request handler.
        GlobalScope.launch (Dispatchers.IO) {
            httpServer = vertx.createHttpServer()
            httpServer
                .requestHandler(router::accept)
                .listen(app.configuration.gateway.port) { result ->
                    if (result.succeeded()) {
                        println("[WEB_GATE] start to listen: ${httpServer.actualPort()}")
                    } else {
                        println("[WEB_GATE] start WebGate failed, caused by ${result.cause().message}")
                    }
                }
        }
    }

    override fun stop() {
        super.stop()
        router.clear()
        println("[WEB_GATE] stop WebGate server")
    }

    ///////////////////////////

    private fun registerRoute(router: Router) {
        registerAuthorityApi(router)
        router.route().handler(FailureHandler())
    }

    private fun registerAuthorityApi(router: Router) {
        router.route("/api/authority/register").handler(::onAuthorityRegister)
        router.route("/api/authority/unregister").handler(::onAuthorityUnregister)
        router.route("/api/authority/forget").handler(::onAuthorityForget)
        router.route("/api/authority/login").handler(::onAuthorityLogin)
        router.route("/api/authority/logout").handler(::onAuthorityLogout)
    }

}

/**
 * Failure handler
 */
class FailureHandler : Handler<RoutingContext> {

    override fun handle(event: RoutingContext?) {
        event ?: return

        val clientHost = event.request().connection().remoteAddress().host()
        val clientPort = event.request().connection().remoteAddress().port()
        val requestUri = event.request().uri()
        println("[WEB_GATE] failure request from $clientHost:$clientPort for $requestUri")
        event.response()
            .putHeader("content-type", "text/plain")
            .setStatusCode(404)
            .setStatusMessage("No service found")
            .end(
                ResponseBuilder().fail(
                    code = 404,
                    message = "No service found"
                ).build()
            )
    }

}