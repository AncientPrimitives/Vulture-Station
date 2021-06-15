package com.cobox.vulture.nasserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureHttpGateway
import com.cobox.vulture.busniess.vertx.FileUploadHandler
import com.cobox.vulture.standard.xutil.FileSystem.canonicalPath
import com.cobox.vulture.standard.xutil.FileSystem.ensureFolder
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class NasGateway(
    vertx: Vertx,
    config: JsonObject?
): VultureHttpGateway(vertx, config) {

    companion object {
        const val TAG = "NasGateway"
    }

    override fun onFillRoutes(): List<(Router) -> Unit> = listOf(
        nasUploadTestPage, // 上传文件测试页
        nasUpload,         // 上传文件
        nasDownload,       // 下载文件
        nasUsage,          // Nas磁盘存储信息
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

    private val nasUploadTestPage: (Router) -> Unit = { router ->
        router.get("/nas/uploadtest").blockingHandler { ctx ->
            ctx
                .response()
                .putHeader("Content-Encoding", "utf-8")
                .putHeader("Content-Type", "text/html")
                .end(
                    """
                        <!DOCTYPE html>
                        <html>
                            <head>
                                <meta charset="utf-8">
                                <title>文件上传测试</title>
                            </head>
                            <body>
                                <h1>文件上传测试</h1>
                                <form action="/nas/upload" method="post" enctype="multipart/form-data">
                                    1. <input type="file" name="upload"> </br>
                                    2. 指定上传的目录：<input type="text" name="desire_path">  </br>
                                    3. <input type="submit">
                                </form>
                            </body>
                        </html>
                    """.trimIndent()
                )
        }
    }

    /**
     * 在NAS上创建一个文件。
     * 离线下载：如指定了download_url，则is_dir会被忽略，下载的内容是一个文件则创建以:filepath命名的文件，下载的内容是多个文件则会创建以:filepath
     * 命名的文件夹来存放下载的文件。
     * 创建空目录：如果未指定download_url，且is_dir=true，则创建一个以:filepath命名的文件夹。
     *
     * url: /nas/create_file/:username/:filepath?download_url={1}&is_dir={2}
     * method: GET | POST
     * params:
     *  - :username, 非空，文件所属的用户
     *  - :filepath, 非空，urlencode, 文件在云盘的路径位置
     *  - is_dir, 非空，值为{ true, false }, 创建的目标是否为文件夹
     *  - download_url, 可空，urlencode, 指定离线下载的url
     * return:
     *  - 创建目录；{ TODO }
     *  - 创建离线下载文件：{ TODO }
     */
    private val nasCreateFile: (Router) -> Unit = { router ->

    }

    /**
     * 向NAS上传文件
     *
     * url: /nas/upload
     * method: POST, multipart/form-data
     * params:
     *  - desire_path, 非空，表单域参数，文件应放置在云盘的路径位置
     *  - tags, 可空，表单域参数，文件的标签（用于分类、检索）
     * return: status code (201)
     */
    private val nasUpload: (Router) -> Unit = { router ->
        eventBus.request<String>("/nas/storage_root", "") { msg ->
            val nasRoot = JsonObject(msg.result().body()).getString("root")
            vertx.ensureFolder(nasRoot)

            router.route("/nas/upload").order(1).handler(
                FileUploadHandler().setUploadsDirectory(vertx.canonicalPath(nasRoot))
            )

            router.post("/nas/upload").order(2).blockingHandler { ctx ->
                kotlin.runCatching {
                    val fileUploads = ctx.fileUploads()
                    val filePathsText = StringBuilder()
                    fileUploads.forEachIndexed { index, file ->
                        filePathsText.append(" - $index. ").append(
                            "${file.fileName()} -> ${file.uploadedFileName()}, " + "size: ${file.size()}, " + "mime type: ${file.contentType()}"
                        ).append("\n")
                    }.let {
                        if (filePathsText.isNotEmpty())
                            filePathsText.deleteAt(filePathsText.lastIndex)
                    }

                    val attributes = ctx.request().formAttributes()
                    val attributesText = StringBuilder()
                    attributes.forEachIndexed { index, attribute ->
                        attributesText.append(" - $index. ").append(
                            "KV: '${attribute.key}' -> '${attribute.value}'"
                        ).append("\n")
                    }.let {
                        if (attributesText.isNotEmpty())
                            attributesText.deleteAt(attributesText.lastIndex)
                    }

                    Log.info(TAG, """
                        [nasUplaod] ${fileUploads.size} files uploaded:
                        $filePathsText
                        ${attributes.size()} attributes:
                        $attributesText
                    """.trimIndent())

                    ctx.response().end()
                }.onFailure {
                    ctx.fail(500, it)
                }
            }

        }
    }

    /**
     * 从NAS下载文件
     *
     * url: /nas/download/:username/:filepath
     * method: GET | POST
     * params:
     *  - :username, 非空，文件所属的用户
     *  - :filepath, 非空，urlencode, 文件在云盘的路径位置
     * return:
     *  { TODO }
     */
    private val nasDownload: (Router) -> Unit = { router ->
        val requestHandler = Handler<RoutingContext> { ctx ->
            val username = ctx.request().params()["username"]
            val filepath = ctx.request().params()["filepath"]
            vertx.fileSystem().readFile(filepath) { result ->
                if (result.succeeded()) {
                    ctx.request().response().end(
                        result.result()
                    )
                } else {
                    ctx.fail(404)
                }
            }
        }
        router.get("/nas/download/:username/:filepath").blockingHandler(requestHandler)
        router.post("/nas/download/:username/:filepath").blockingHandler(requestHandler)
    }

    /**
     * 列举指定目录的内容
     *
     * url: /nas/upload/:username/:filepath
     * method: GET | POST
     * params:
     *  - :username, 非空，文件所属的用户
     *  - :filepath, 非空，urlencode, 文件在云盘的路径位置
     * return:
     *  - 如果:filepath是个有效目录，则 { TODO }
     *  - 如果:filepath无效或是个文件，则 { TODO }
     */
    private val nasList: (Router) -> Unit = { router ->

    }

    /**
     * NAS存储量的用度。
     * 如果携带了:username则指定查看某位用户的NAS存储用度，如不指定则查看服务器NAS的全局存储用度
     *
     * url: /nas/usage/:username
     * method: GET | POST
     * params:
     *  - :username, 可空，指定查看某位用户的NAS存储用度，如不指定则查看服务器NAS的全局存储用度
     * return:
     *  - 指定用户用度的返回结果：{ TODO }
     *  - 全局用度的返回结果：{ TODO }
     */
    private val nasUsage: (Router) -> Unit = { router ->
        val requestHandler = Handler<RoutingContext> { ctx ->
            ctx.request().let { request ->
                eventBus.request<String>("/nas/usage", "") { result ->
                    if (result.failed()) {
                        Log.error(TAG, "[nasUsage] no server reply for '/nas/usage'")
                        request.response().let { response ->
                            response.statusCode = 500
                            response.end("Nas server isn't deploy")
                        }
                        return@request
                    }

                    request.response().end(result.result().body())
                }
            }
        }

        router.get("/nas/usage").blockingHandler(requestHandler)
        router.post("/nas/usage").blockingHandler(requestHandler)
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