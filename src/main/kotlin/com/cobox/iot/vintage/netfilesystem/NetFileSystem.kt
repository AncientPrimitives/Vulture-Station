package com.cobox.iot.vintage.netfilesystem

import com.cobox.iot.vulture.companion.NFS
import com.cobox.iot.vulture.webgate.ResponseBuilder
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

object NetFileSystemRoutingHandler {

    private val service = NetFileSystem()

    fun login(context: RoutingContext) {
        context.request().params().let { params ->
            val username = params["username"]
            val password = params["secret"]
            println("[NFS] login($username, $password)")

            GlobalScope.launch (Dispatchers.IO) {
                if (
                    (username.toLowerCase() == NFS.sudor.toLowerCase())
                    and (password == NFS.secret)
                ) {
                    context.response().end(
                        ResponseBuilder().okay(
                            payload = JsonObject().apply {
                                put("token", NFS.sudorToken)
                            }
                        ).build()
                    )
                } else {

                    // Fixme 分配一个新的token，并加入store

                    context.response().end(
                        ResponseBuilder().fail(
                            code = 403,
                            message = "Permission denied"
                        ).build()
                    )
                }
            }
        }
    }

    fun logout(context: RoutingContext) {
        context.request().let { request ->
            val token = checkToken(context)
            token ?: return@let

            if (token != NFS.sudorToken) {
                // Fixme 从store移除token
            }
        }
    }

    fun dir(context: RoutingContext) {
        context.request().let { request ->
            val token = checkToken(context)
            token ?: return@let

            request.bodyHandler { body ->
                body.toJsonObject().let { json ->
                    json.getString("path", null)?.let { path ->
                        println("[NFS] dir(token=$token, path='$path')")

                        GlobalScope.launch (Dispatchers.IO) {
                            context.response().end(
                                ResponseBuilder().okay(
                                    payload = JsonObject().apply {
                                        put("dir", JsonArray().apply {
                                            service.dir(path).forEach { fileStruct ->
                                                this.add(fileStruct.path)
                                            }
                                        })
                                    }
                                ).build()
                            )
                        }
                    } ?: let {
                        context.response().end(
                            ResponseBuilder().fail(
                                code = 406,
                                message = "Cannot access this path"
                            ).build()
                        )
                    }
                }
            }
        }
    }

    fun open(context: RoutingContext) {
        context.request().let { request ->
            val token = checkToken(context)
            token ?: return@let

            request.bodyHandler { body ->
                body.toJsonObject().let { json ->
                    json.getString("path", null)?.let { path ->
                        println("[NFS] open(token=$token, path='$path')")

                        GlobalScope.launch (Dispatchers.IO) {
                            service.open(path)?.let { physicalPath ->
                                context.response().sendFile(physicalPath)
                            } ?: let {
                                context.response().end(
                                    ResponseBuilder().fail(
                                        code = 406,
                                        message = "Cannot access this path"
                                    ).build()
                                )
                            }
                        }
                    } ?: let {
                        context.response().end(
                            ResponseBuilder().fail(
                                code = 406,
                                message = "Cannot access this path"
                            ).build()
                        )
                    }
                }
            }
        }
    }

    fun preview(context: RoutingContext) {
        context.request().let { request ->
            val path = request.params()["path"]
            println("[NFS] preview(path='$path')")
            GlobalScope.launch (Dispatchers.IO) {
                service.open(path)?.let { physicalPath ->
                    context.response().sendFile(physicalPath)
                } ?: let {
                    context.response().end(
                        ResponseBuilder().fail(
                            code = 406,
                            message = "Cannot access this path"
                        ).build()
                    )
                }
            }
        }
    }

    fun usage(context: RoutingContext) {
        context.request().let { request ->
            val token = checkToken(context)
            token ?: return@let

            request.bodyHandler { body ->
                println("[NFS] mount(token=$token)")

                GlobalScope.launch (Dispatchers.IO) {
                    service.usage().let { info ->
                        context.response().end(
                            ResponseBuilder().okay(
                                payload = JsonObject().apply {
                                    put("mount", JsonObject(info))
                                }
                            ).build()
                        )
                    }
                }
            }
        }
    }

    private fun checkToken(context: RoutingContext): String? {
        val token = context.request().headers()["token"]
        return if (token != NFS.sudorToken) {

            // Fixme 检查token是否在store中

            context.response().end(
                ResponseBuilder().fail(
                    code = 403,
                    message = "Permission denied"
                ).build()
            )
            null
        } else {
            token
        }
    }

}


class NetFileSystem {

    data class FileStruct (
        val path: String
    )

    fun dir(path: String): List<FileStruct>
        = File(NFS.rootDir + path).let {
            if (!it.exists()) {
                emptyList()
            } else {
                it.listFiles { dir, name ->
                    dir.path.startsWith(NFS.rootDir, ignoreCase = true)
                }?.map { file ->
                    FileStruct(
                        path = file.path.removePrefix(NFS.rootDir)
                    )
                } ?: emptyList()
            }
        }

    fun open(path: String): String?
        = File(NFS.rootDir + path).let {
            if (!it.exists()) {
                null
            } else {
                it.absolutePath
            }
        }

    fun usage(): Map<String, Long>
        = File(NFS.rootDir).let { root ->
            mapOf(
                "totalSpace" to root.totalSpace,
                "freeSpace" to root.freeSpace,
                "usableSpace" to root.usableSpace
            )
        }

}