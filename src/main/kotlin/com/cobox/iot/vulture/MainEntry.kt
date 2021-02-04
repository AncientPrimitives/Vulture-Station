package com.cobox.iot.vulture

import com.cobox.iot.vulture.application.Configuration
import io.vertx.core.Vertx

object MainEntry {

    @JvmStatic
    fun main(args: Array<String>) {
        println("===== Vulture Station =====")
        val config = Configuration.Builder().load("${System.getProperty("user.dir", "/")}/config/vulture.config")
        val app = VultureApplication(config)
        val maintainer = com.cobox.iot.vulture.maintain.Maintainer(app)
        val deviceCloud = com.cobox.iot.vintage.devicecloud.DeviceCloud(app)
        val gateway = com.cobox.iot.vulture.webgate.GateWay(app)
        val vertex = Vertx.vertx().apply {
            deployVerticle(maintainer)
            deployVerticle(deviceCloud)
            deployVerticle(gateway)
        }
        println("===== 完成部署 =====")

        Runtime.getRuntime().addShutdownHook(
            Thread{
                vertex.apply {
                    println("===== 卸载部署 =====")
                    undeploy(maintainer.deploymentID())
                    undeploy(deviceCloud.deploymentID())
                    undeploy(gateway.deploymentID())

                    println("===== 关闭服务 =====")
                    close()
                }

                kotlin.runCatching {
                    Thread.currentThread().join()
                }
                app.close()
            }
        )
    }

}