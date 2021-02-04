package com.cobox.iot.vulture.webgate

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.companion.NFS
import com.cobox.iot.vulture.lang.emptyJsonObject
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface AuthorityApi {
    fun onAuthorityRegister(context: RoutingContext)
    fun onAuthorityUnregister(context: RoutingContext)
    fun onAuthorityForget(context: RoutingContext)
    fun onAuthorityLogin(context: RoutingContext)
    fun onAuthorityLogout(context: RoutingContext)
}

class AuthorityApiImpl(
    val app: Application
) : AuthorityApi {

    /**
     * Api: /api/authority/register
     * Param:
     *     json: {
     *         username: plaintext,
     *         secret: Base64
     *     }
     * Method:
     *     GET for debugging
     *     POST
     * Return:
     *     json: {
     *         state: {
     *             code: 200,
     *             result: "OK"
     *         },
     *         message: {},
     *         payload: {
     *             requestId: 000000,
     *             token: "xxxx"
     *         }
     *     }
     */
    override fun onAuthorityRegister(context: RoutingContext) {
        // TODO("Not yet implemented")
        // 1. 确认此用户是否已存在
        // 2. 通知NAS和IOT创建账户
        // 3. 携带nas_id和iot_id创造用户
        val request = context.request()
        readAccountParamsForLogin(request) { username, secret, token ->
            println("[AUTH] register($username, $secret, $token)")

            GlobalScope.launch (Dispatchers.IO) {
                if (
                    (username.toLowerCase() == NFS.sudor.toLowerCase())
                    and (secret == NFS.secret)
                ) {
                    context.response()
                        .setStatusCode(406)
                        .setStatusMessage("User has already existed")
                        .end()
                } else {
                    app.authority.register(username, secret).let { result ->
                        if (result.isGranted) {
                            context.response()
                                .setStatusCode(201)
                                .setStatusMessage("Created user account")
                                .end(
                                    ResponseBuilder().okay(
                                        code = 201,
                                        message = "Created user account",
                                        payload = JsonObject().apply {
                                            put("token", result.token)
                                        }
                                    ).build()
                                )
                        } else {
                            context.response()
                                .setStatusCode(406)
                                .setStatusMessage("User has already existed")
                                .end()
                        }
                    }
                }
            }

        }
    }

    /**
     * Api: /api/authority/unregister
     * Headers:
     *     token: "xxxxx"
     * Method:
     *     GET for debugging
     *     POST
     * Return:
     *     json: {
     *         state: {
     *             code: 200,
     *             result: "OK"
     *         }
     *     }
     */
    override fun onAuthorityUnregister(context: RoutingContext) {
        TODO("Not yet implemented")
    }

    override fun onAuthorityForget(context: RoutingContext) {
        TODO("Not yet implemented")
    }

    /**
     * Api: /api/authority/login
     * Param:
     *     json: {
     *         username: plaintext,
     *         secret: Base64
     *     }
     * Method:
     *     GET for debugging
     *     POST
     * Return:
     *     json: {
     *         state: {
     *             code: 200,
     *             result: "OK"
     *         },
     *         message: {},
     *         payload: {
     *             requestId: 000000,
     *             token: "xxxx"
     *         }
     *     }
     */
    override fun onAuthorityLogin(context: RoutingContext) {
        val request = context.request()
        readAccountParamsForLogin(request) { username, secret, token ->
            println("[AUTH] login($username, $secret, $token)")

            GlobalScope.launch (Dispatchers.IO) {
                if (
                    (username.toLowerCase() == NFS.sudor.toLowerCase())
                    and (secret == NFS.secret)
                ) {
                    context.response()
                        .setStatusCode(200)
                        .setStatusMessage("OK")
                        .end(
                            ResponseBuilder().okay(
                                payload = JsonObject().apply {
                                    put("token", NFS.sudorToken)
                                }
                            ).build()
                        )
                } else {
                    // Fixme 分配一个新的token，并加入store
                    // 1. 查询认证数据库，看是否存在有效token，有则返回
                    // 2. 如无有效token，新分配一个token，并存入认证数据库
                    // 3. 返回token
                    val authenticatedToken = if (token.isEmpty()) {
                        app.authority.authority(username, secret).token
                    } else {
                        app.authority.authority(token).token
                    }

                    if (authenticatedToken.isEmpty()) {
                        context.response()
                            .setStatusCode(403)
                            .setStatusMessage("Permission denied")
                            .end(
                                ResponseBuilder().fail(
                                    code = 403,
                                    message = "Permission denied"
                                ).build()
                            )
                    } else {
                        context.response()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .end(
                                ResponseBuilder().okay(
                                    payload = JsonObject().apply {
                                        put("token", authenticatedToken)
                                    }
                                ).build()
                            )
                    }
                }
            }
        }
    }

    /**
     * Api: /api/authority/logout
     * Headers:
     *     token: "xxxxx"
     * Method:
     *     GET for debugging
     *     POST
     * Return:
     *     json: {
     *         state: {
     *             code: 200,
     *             result: "OK"
     *         }
     *     }
     */
    override fun onAuthorityLogout(context: RoutingContext) {
        val request = context.request()
        readAccountParamsForLogin(request) { _, _, token ->
            println("[AUTH] logout($token)")

            GlobalScope.launch (Dispatchers.IO) {
                if ((token == NFS.sudorToken) or app.authority.unauthority(token)) {
                    context.response()
                        .setStatusCode(200)
                        .setStatusMessage("OK")
                        .end()
                } else  {
                    context.response()
                        .setStatusCode(403)
                        .setStatusMessage("Permission denied")
                        .end()
                }
            }
        }
    }

    //////////////////////////////////////////

    private fun readAccountParamsForLogin(
        request: HttpServerRequest,
        callback: (username: String, secret: String, token: String) -> Unit
    ) {
        when (request.method()) {
            HttpMethod.GET -> callback(
                if (request.params().contains("username")) request.params()["username"] else "",
                if (request.params().contains("secret")) request.params()["secret"] else "",
                if (request.headers().contains("token")) request.headers()["token"] else ""
            )

            HttpMethod.POST -> {
                request.bodyHandler { buffer ->
                    val json = kotlin.runCatching { buffer.toJsonObject() }.getOrDefault(emptyJsonObject())
                    callback(
                        json.getString("username", ""),
                        json.getString("secret", ""),
                        if (request.headers().contains("token")) request.headers()["token"] else ""
                    )
                }
            }

            else -> callback("", "", "")
        }
    }

}