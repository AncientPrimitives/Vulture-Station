package com.cobox.vulture

import com.cobox.vulture.system.LinuxCpuMonitorTest
import com.cobox.utilites.log.Log
import com.cobox.vulture.standard.xutil.Text
import com.cobox.vulture.standard.xutil.Text.EMPTY_TEXT
import com.hazelcast.config.FileSystemXmlConfig
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.io.PrintStream

const val TAG = "Main"
const val Version = "1.1.0"

class Vulture {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            LinuxCpuMonitorTest()
            Vulture().deploy()
        }
    }

    private data class VultureServerDeployment (
        val name: String,
        val clazz: Class<out Verticle>,
        val enable: Boolean = VultureConfig.Default.ENABLE,
        val instances: Int = VultureConfig.Default.INSTANCE,
        val config: String = Text.EMPTY_TEXT,
        val comment: String = Text.EMPTY_TEXT
    )

    init {
        Log.info(TAG, "[main] ===== Vulture =====")
        ManagementFactory.getRuntimeMXBean().let { mx ->
            Log.info(TAG, "[main] Vulture $Version running at ${mx.vmName} (build ${mx.vmVersion}) ${mx.vmVendor}")
            Log.info(TAG, "[main] JVM is specified by Java ${mx.specVersion} of ${mx.specVendor}")
        }
    }

    private fun checkDeploymentInfo(configFile: String): List<VultureServerDeployment> {
        val deploymentVerticleSet = File(configFile).let { file ->
            if (!file.exists()) {
                Log.error(TAG, "[checkDeploymentInfo] deployment config file '$configFile' no found, shutdown!!")
                return emptyList()
            }

            JsonObject(
                FileInputStream(file).use {
                    val content = StringBuilder()
                    val buffer = ByteArray(1024)
                    val fileLength = it.available()
                    var filePosition = 0

                    do {
                        val readCount = it.read(buffer).apply {
                            if (this > 0) {
                                content.append(
                                    String(buffer, 0, this)
                                )
                            }
                        }
                        filePosition += readCount
                    } while (filePosition < fileLength)

                    content.trim().toString()
                }
            ).let { json ->
                kotlin.runCatching {
                    json.getJsonArray(VultureConfig.Key.DEPLOYMENT).let { deployments ->
                        val deployVerticles = mutableListOf<VultureServerDeployment>()
                        val deploymentsCount = deployments.size()
                        for (i in 0 until deploymentsCount) {
                            deployments.getJsonObject(i).let { deployment ->
                                deployVerticles.add(
                                    VultureServerDeployment(
                                        name = deployment.getString(VultureConfig.Key.VERTICLE),
                                        clazz = Class.forName(deployment.getString(VultureConfig.Key.VERTICLE)) as Class<out Verticle>,
                                        enable = deployment.getBoolean(VultureConfig.Key.ENABLE, VultureConfig.Default.ENABLE),
                                        instances = deployment.getInteger(VultureConfig.Key.INSTANCE, VultureConfig.Default.INSTANCE),
                                        config = deployment.getString(VultureConfig.Key.CONFIG) ?: EMPTY_TEXT,
                                        comment = deployment.getString(VultureConfig.Key.COMMENT) ?: EMPTY_TEXT,
                                    )
                                )
                            }
                        }

                        deployVerticles
                    }
                }.getOrElse {
                    Log.error(TAG, "[checkDeploymentInfo] something wrong occurs in deployment config file '$configFile', shutdown", it)
                    emptyList()
                }
            }
        }

        if (deploymentVerticleSet.isEmpty()) {
            Log.error(TAG, "[checkDeploymentInfo] No VultureServer deployment info found")
        } else {
            Log.info(TAG, "[checkDeploymentInfo] ${deploymentVerticleSet.size} VultureServer found:")
            deploymentVerticleSet.forEachIndexed { index, deployment ->
                Log.info(
                    TAG,
                    "[checkDeploymentInfo] $index. { ${deployment.name}, instance: ${deployment.instances}, enable: ${deployment.enable}, config: '${deployment.config}' }"
                )
            }
        }

        return deploymentVerticleSet
    }

    private fun Vertx.deployVerticle(
        verticle: VultureServerDeployment,
        completionHandler: Handler<AsyncResult<String>>? = null
    ) {
        if (verticle.config.isNotEmpty() && fileSystem().existsBlocking(verticle.config)) {
            Log.info(TAG, "[deploy] found config file from ${File(verticle.config).canonicalPath}, deploy ${verticle.name} with it")
            ConfigRetriever.create(
                this,
                ConfigRetrieverOptions().addStore(
                    ConfigStoreOptions()
                        .setFormat("json")
                        .setType("file")
                        .setConfig(JsonObject().put("path", verticle.config))
                )
            ).getConfig { config ->
                deployVerticle(
                    verticle.clazz,
                    DeploymentOptions().apply {
                        this.config = config.result()
                        this.instances = verticle.instances
                    },
                    completionHandler
                )
            }
        } else {
            if (verticle.config.isEmpty()) {
                Log.info(TAG, "[deploy] no config file, deploy ${verticle.name} with default config")
            } else {
                Log.info(TAG, "[deploy] no found config file from ${File(verticle.config).canonicalPath}, deploy ${verticle.name} with default config")
            }

            deployVerticle(
                verticle.clazz,
                DeploymentOptions().apply {
                    this.instances = verticle.instances
                },
                completionHandler
            )
        }
    }

    fun deploy() {
        setupLogoutChannel()
        val deployments = checkDeploymentInfo(VultureConfig.Default.VULTURE_CONFIG_FILE)
        if (deployments.isEmpty()) return

        Vertx.clusteredVertx(
            VertxOptions().apply {
                clusterManager = if (File(VultureConfig.Default.CLUSTER_CONFIG_FILE).exists()) {
                    HazelcastClusterManager(FileSystemXmlConfig("./config/cluster.xml"))
                } else {
                    HazelcastClusterManager()
                }
            }
        ).onSuccess { vertx ->
            Log.info(TAG, "[deploy] Clustered Vert.x for Vulture is created, version is $Version")
            Log.info(TAG, "[deploy] Current workspace is ${System.getProperty("user.dir")}")

            deployments.forEach { component ->
                if (!component.enable) {
                    Log.info(TAG, "[deploy] ${component.name} is disabled, skip")
                    return@forEach
                }

                vertx.executeBlocking<Void> {
                    vertx.deployVerticle(component) { result ->
                        if (result.succeeded()) {
                            Log.info(TAG, "[deploy] Deploy ${component.name} succeeded")
                        } else {
                            Log.error(TAG, "[deploy] Deploy ${component.name} failed, caused by ${result.cause()}", result.cause())
                        }
                    }
                }
            }
        }
    }

    private fun setupLogoutChannel() {
        File("./logout").let { log ->
            if (log.exists()) {
                System.setOut(PrintStream(FileOutputStream(log, true)))
            }
        }
    }

}

