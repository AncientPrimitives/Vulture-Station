package com.cobox.vulture.standard.xutil

import com.cobox.utilites.filesystem.FS
import io.vertx.core.Future
import io.vertx.core.Vertx

object FileSystem {

    fun Vertx.ensureFolder(path: String, replaceIfExists: Boolean = false): Future<Boolean> =
        runCatching {
            FS.ensureFolder(path, replaceIfExists)
        }.let { result ->
            return if (result.isSuccess) {
                Future.succeededFuture(true)
            } else {
                Future.failedFuture(result.exceptionOrNull())
            }
        }

    fun Vertx.canonicalPath(path: String): String =
        FS.canonicalPath(path)

}