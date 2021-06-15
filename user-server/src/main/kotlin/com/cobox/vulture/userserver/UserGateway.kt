package com.cobox.vulture.userserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureHttpGateway
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.impl.HttpStatusException
import java.util.*

class UserGateway(
    vertx: Vertx,
    config: JsonObject?
) : VultureHttpGateway(vertx, config) {

    companion object {
        const val TAG = "UserGateway"
    }

    override fun onConfigRoute(route: Route) {
        config
            .getJsonObject(VultureConfig.Key.GATEWAY)
            ?.getString(VultureConfig.Key.VIRTUAL_HOST, VultureConfig.Default.VIRTUAL_HOST)
            ?.let { virtualHost ->
                route.virtualHost(virtualHost)
            }
    }

    private val authentication by lazy {
        // BasicAuthHandler.create(AuthRoom.makeAuthenticationProvider(vertx))

        BasicAuthHandler.create { credentials, resultHandler ->
            data class UsernamePassword(val username: String, val password: String)
            val account = when(credentials) {
                is UsernamePasswordCredentials -> {
                    UsernamePassword(credentials.username, credentials.password)
                }
                is JsonObject -> {
                    UsernamePassword(credentials.getString("username"), credentials.getString("password"))
                }
                else -> null
            }

            account?.apply {
                eventBus.request<String>(
                    "/user/auth/",
                    JsonObject()
                        .put("username", account.username)
                        .put("password", account.password)
                        .toString()
                ) { result ->
                    if (result.succeeded()) {
                        JsonObject(result.result().body()).getString("token").let { token ->
                            Log.info(TAG, "[Authentication] '${username}:${password}' authenticated as $token")
                            resultHandler.handle(
                                Future.succeededFuture(
                                    User.fromToken(token).apply {
                                        principal().put("username", account.username)
                                        principal().put("password", account.password)
                                    }
                                )
                            )
                        }
                    }
                }
            } ?: let {
                resultHandler.handle(Future.failedFuture(HttpStatusException(400)))
            }
        }
    }

    override fun onFillRoutes(): List<(Router) -> Unit> = listOf(
        authConversion, /* private */
        auth,
        defaultRoute
    )

    private val authConversion: (Router) -> Unit = { router ->
        router.route("/auth/:user/:secret").order(1).handler { ctx ->
            val headers = ctx.request().headers()
            headers[HttpHeaders.AUTHORIZATION].isNullOrBlank().let { hasAuthorization ->
                if (!hasAuthorization) {
                    val username = ctx.pathParam("user")
                    val password = ctx.pathParam("secret")
                    val authorization = String(Base64.getEncoder().encode(
                        "$username:$password".encodeToByteArray()
                    ))
                    headers[HttpHeaders.AUTHORIZATION] = "Basic $authorization"
                }

                authentication.handle(ctx)
            }
        }
    }

    private val auth: (Router) -> Unit = { router ->
        router.get("/auth/:user/:secret").order(2).blockingHandler { ctx ->
            val user = ctx.user()
            val isAuthenticated = (ctx.user() != null)
            Log.info(
                TAG,
                "[Authentication] '${user.get<String>("username")}:${user.get<String>("password")}' authenticate " +
                        if (isAuthenticated) "passed" else "blocked"
            )
            ctx.json(ctx.user().principal())
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