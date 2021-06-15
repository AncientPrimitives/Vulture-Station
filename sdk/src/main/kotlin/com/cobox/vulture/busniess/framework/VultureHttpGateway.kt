package com.cobox.vulture.busniess.framework

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router

abstract class VultureHttpGateway(
    vertx: Vertx,
    configuration: JsonObject?
): VultureGateway(vertx) {

    private val routes by lazy {
        onFillRoutes()
    }

    protected val config: JsonObject = configuration ?: JsonObject()

    var name: String? = null
    var routePoint: Route? = null

    open val router: Router by lazy {
        Router.router(vertx).let { router ->
            routes.forEach { route ->
                route(router)
            }

            // 如无任何路由处理，则返回最终异常结果
            router
        }
    }

    open protected fun onConfigRoute(route: Route) {}

    abstract fun onFillRoutes(): List<(Router) -> Unit>

    fun mount(route: Route) {
        routePoint = route
        route.subRouter(router)
        onConfigRoute(route)
    }

    fun unmount() {
        routePoint?.remove()
        routePoint = null
    }

}