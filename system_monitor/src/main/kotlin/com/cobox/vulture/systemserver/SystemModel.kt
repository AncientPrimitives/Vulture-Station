package com.cobox.vulture.systemserver

import com.cobox.vulture.busniess.framework.VultureServerModel
import com.cobox.vulture.system.SystemMonitor
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class SystemModel(
    vertx: Vertx,
    private val config: JsonObject
) : VultureServerModel(vertx) {

    companion object {
        const val TAG = "SystemModel"
        private const val NO_JOB = 0L
        private const val REFRESH_INTERVAL = 1000L
    }

    val systemMonitor = SystemMonitor()
    private var refreshInterval = VultureConfig.Default.MONITOR_REFRESH_INTERVAL
    private var monitorDaemonJob: Long = NO_JOB

    override fun onPrepareCache() {
        refreshInterval = config.getLong(VultureConfig.Key.MONITOR_REFRESH_INTERVAL, VultureConfig.Default.MONITOR_REFRESH_INTERVAL)
        monitorDaemonJob = vertx.setPeriodic(REFRESH_INTERVAL) { jobId ->
            vertx.executeBlocking<Void> { job ->
                systemMonitor.refresh()
                job.complete()
            }
        }
        systemMonitor.refresh()
    }

    override fun onClearCache() {
        vertx.cancelTimer(monitorDaemonJob)
        monitorDaemonJob = NO_JOB
    }

}