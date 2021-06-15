package com.cobox.vulture.nasserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureServerModel
import com.cobox.vulture.database.VultureConfig
import com.cobox.vulture.standard.xutil.FileSystem.ensureFolder
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class NasModel(
    vertx: Vertx,
    private val config: JsonObject
) : VultureServerModel(vertx) {

    companion object {
        const val TAG = "NasModel"
    }

    data class Usage(
        val usable: Long,
        val total: Long
    )

    var repo: String = ""

    val usage: Usage
        get() {
            return if (repo.isNotEmpty()) {
                vertx.fileSystem().let { fs ->
                    fs.fsPropsBlocking(repo).let { props ->
                        Usage(
                            usable = props.usableSpace(),
                            total = props.totalSpace()
                        )
                    }
                }
            } else {
                Usage(0, 0)
            }
        }

    override fun onPrepareCache() {
        repo = config.getString(VultureConfig.Key.DATABASE_REPO, com.cobox.vulture.nasserver.VultureConfig.Default.NAS_REPO)
        vertx.ensureFolder(repo).onComplete {
            if (it.failed()) {
                Log.error(TAG, "[ensureFolder] create nas root folder '$repo' failed, caused by ${it.cause()}", it.cause())
            }
        }
    }

}