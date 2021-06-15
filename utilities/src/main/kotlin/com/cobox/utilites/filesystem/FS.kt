package com.cobox.utilites.filesystem

import java.io.File
import java.lang.RuntimeException

object FS {

    fun ensureFolder(path: String, replaceIfExists: Boolean = false) {
        File(path).let { file ->
            when {
                (file.exists() && file.isFile) -> {
                    if (replaceIfExists) {
                        file.deleteRecursively()
                    } else {
                        throw RuntimeException("folder isn't a directory, cannot re-create as a folder")
                    }
                    file.mkdirs()
                }
                (!file.exists()) -> {
                    file.mkdirs()
                }
                else -> { }
            }
        }
    }

    fun canonicalPath(path: String): String =
        File(path).let { file ->
            file.canonicalPath
        }

}