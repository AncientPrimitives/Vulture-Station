package com.cobox.iot.vulture.application

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.FileInputStream

private val defaultConfig = Configuration(
    gateway = Configuration.Gateway(
        port = 80
    ),
    database = Configuration.Database(
        // 不能public直接使用，需要转换为绝对路径
        root = System.getProperty("user.dir", "/") + "/databases/"
    ),
    nas = Configuration.Nas(
        // 不能public直接使用，需要转换为绝对路径
        root = System.getProperty("user.dir", "/") + "/repo/nas/"
    )
)

private fun convertJsonToConfiguration(json: JsonObject): Configuration =
    Configuration(
        gateway = Configuration.Gateway(
            port = if (json.containsKey("gateway")) {
                json.getJsonObject("gateway")
                    .getInteger("port", defaultConfig.gateway.port)
            } else defaultConfig.gateway.port
        ),
        database = Configuration.Database(
            root = if (json.containsKey("database")) {
                File(
                    json.getJsonObject("database")
                        .getString("root", defaultConfig.database.root)
                ).canonicalPath
            } else File(defaultConfig.database.root).canonicalPath
        ),
        nas = Configuration.Nas(
            root = if (json.containsKey("nas")) {
                File(
                    json.getJsonObject("nas")
                        .getString("root", defaultConfig.nas.root)
                ).canonicalPath
            } else File(defaultConfig.nas.root).canonicalPath
        )
    )

data class Configuration(
    val gateway: Gateway,
    val database: Database,
    val nas: Nas
) {

    data class Gateway(
        val port: Int
    )

    data class Database(
        val root: String
    )

    data class Nas(
        val root: String
    )

    class Builder {

        fun load(filePath: String): Configuration =
            File(filePath).let { configFile ->
                val isConfigExists = configFile.exists()
                val isConfigFileValid = configFile.isFile
                val isConfigFileCanRead = configFile.canRead()

                println("[CONFIG] load config from '${configFile.canonicalPath}'")
                if (!isConfigExists) {
                    println("[CONFIG] '${configFile.canonicalPath}' isn't exists, load default config")
                    return defaultConfig
                }
                if (!isConfigFileValid) {
                    println("[CONFIG] '${configFile.canonicalPath}' is not a file, load default config")
                    return defaultConfig
                }
                if (!isConfigFileCanRead) {
                    println("[CONFIG] permission denied, '${configFile.canonicalPath}' cannot be read, load default config")
                    return defaultConfig
                }

                return runCatching<Configuration> {
                        FileInputStream(configFile).use { fin ->
                            JsonObject(
                                Buffer.buffer().apply {
                                    val buffer = ByteArray(16 * 1024)
                                    var readCount: Int
                                    do {
                                        readCount = fin.read(buffer)
                                        appendBytes(buffer)
                                    } while (readCount > 0)
                                }
                            ).let { json ->
                                convertJsonToConfiguration(json)
                            }
                        }
                    }.onFailure {
                        println("[CONFIG] load default config, '${configFile.canonicalPath}' read failed, caused by ${it.cause}")
                        it.printStackTrace()
                    }.getOrDefault(defaultConfig)
            }

    }

}