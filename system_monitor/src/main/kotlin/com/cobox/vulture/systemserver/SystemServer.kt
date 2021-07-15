package com.cobox.vulture.systemserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.busniess.framework.VultureServer
import com.cobox.vulture.system.SystemMonitor
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class SystemServer : VultureServer() {

    companion object {
        const val TAG = "SystemServer"
    }

    private var gatewayName: String? = null
    private val systemModel by lazy {
        SystemModel(
            vertx,
            config().getJsonObject(VultureConfig.Key.SERVER)
        )
    }

    override fun onServicePrepare() {
        systemModel.prepareCache()
        createHttpGateway(SystemGateway::class.java.name, config()) { result ->
            if (result.succeeded()) {
                gatewayName = result.result()
            } else {
                Log.error(TAG, "[onServicePrepare] register gateway failed, caused by ${result.cause()}", result.cause())
            }
        }
    }

    private fun generateTempInfo(monitor: SystemMonitor): JsonObject =
        JsonObject().put(
            "temperatures",
            JsonArray().apply {
                val sensorCount = monitor.getTemperatureCount()
                for (slot in 0 until sensorCount) {
                    add(JsonObject().put("temperature", monitor.getTemperature(slot)))
                }
            }
        )

    private fun generateMemInfo(monitor: SystemMonitor): JsonObject =
        JsonObject()
            .put("totalMem", monitor.getTotalRam())
            .put("availableMem", monitor.getAvailableRam())
            .put("freeMem", monitor.getFreeRam())
            .put("totalSwap", monitor.getTotalSwap())
            .put("freeSwap", monitor.getFreeSwap())

    private fun generateCpuInfo(monitor: SystemMonitor): JsonObject =
        JsonObject().let { json ->
            val coreCount = monitor.getCoreSize()
            json.put("coreCount", coreCount)
            json.put("totalUsage", monitor.getTotalCpuUsage())
            json.put("avgTotalUsage", monitor.getTotalCpuAvgUsage())
            json.put("systemUsage", monitor.getSystemCpuUsage())
            json.put("avgSystemUsage", monitor.getSystemCpuAvgUsage())
            json.put("userUsage", monitor.getUserCpuUsage())
            json.put("avgUserUsage", monitor.getUserCpuAvgUsage())
            json.put("freeUsage", monitor.getFreeCpuUsage())
            json.put("avgFreeUsage", monitor.getFreeCpuAvgUsage())
            if (coreCount > 0) {
                json.put(
                    "cores",
                    JsonArray().apply {
                        for (slot in 1..coreCount) {
                            add(
                                JsonObject()
                                    .put("coreId", slot)
                                    .put("totalUsage", monitor.getTotalCpuUsage(slot))
                                    .put("avgTotalUsage", monitor.getTotalCpuAvgUsage(slot))
                                    .put("systemUsage", monitor.getSystemCpuUsage(slot))
                                    .put("avgSystemUsage", monitor.getSystemCpuAvgUsage(slot))
                                    .put("userUsage", monitor.getUserCpuUsage(slot))
                                    .put("avgUserUsage", monitor.getUserCpuAvgUsage(slot))
                                    .put("freeUsage", monitor.getFreeCpuUsage(slot))
                                    .put("avgFreeUsage", monitor.getFreeCpuAvgUsage(slot))
                            )
                        }
                    }
                )
            }
            json
        }

    override fun onRegisterService() {
        eventBus.consumer<String>("/system/cpuinfo") { msg ->
            val result = generateCpuInfo(systemModel.systemMonitor).toString()
            msg.reply(result)
            Log.info(TAG, "[consumer] /system/cpuinfo: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/system/cpuinfo' on event bus")

        eventBus.consumer<String>("/system/meminfo") { msg ->
            val result = generateMemInfo(systemModel.systemMonitor).toString()
            msg.reply(result)
            Log.info(TAG, "[consumer] /system/meminfo: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/system/meminfo' on event bus")

        eventBus.consumer<String>("/system/tempinfo") { msg ->
            val result = generateTempInfo(systemModel.systemMonitor).toString()
            msg.reply(result)
            Log.info(TAG, "[consumer] /system/tempinfo: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/system/tempinfo' on event bus")

        eventBus.consumer<String>("/system/summary") { msg ->
            val monitor = systemModel.systemMonitor
            val result = JsonObject()
                .put("cpuinfo", generateCpuInfo(monitor))
                .put("meminfo", generateMemInfo(monitor))
                .put("tempinfo", generateTempInfo(monitor))
                .toString()

            msg.reply(result)
            Log.info(TAG, "[consumer] /system/summary: $result")
        }
        Log.info(TAG, "[onRegisterService] listener '/system/summary' on event bus")
    }

    override fun onServiceStart() {

    }

    override fun onServiceStop() {

    }

    override fun onUnregisterService() {

    }

    override fun onServiceDestroy() {
        gatewayName?.let {
            destroyHttpGateway(it) { result ->
                if (result.failed()) {
                    Log.error(TAG, "[onServiceDestroy] unregister gateway failed, caused by ${result.cause()}", result.cause())
                }
            }
        }
        systemModel.clearCache()
    }

}