package com.cobox.vulture.system

import com.cobox.utilites.unit.MB

interface CpuMonitor {
    fun getCoreSize(): Int = 0
    fun getTotalCpuUsage(slot: Int = 0): Float = 0.0f
    fun getUserCpuUsage(slot: Int = 0): Float = 0.0f
    fun getSystemCpuUsage(slot: Int = 0): Float = 0.0f
    fun getFreeCpuUsage(slot: Int = 0): Float = 0.0f
    fun getTotalCpuAvgUsage(slot: Int = 0): Float = 0.0f
    fun getUserCpuAvgUsage(slot: Int = 0): Float = 0.0f
    fun getSystemCpuAvgUsage(slot: Int = 0): Float = 0.0f
    fun getFreeCpuAvgUsage(slot: Int = 0): Float = 0.0f
    fun refreshCpuInfo()
}

interface MemoryMonitor {
    fun getTotalRam(): Long = 0L
    fun getFreeRam(): Long = 0L
    fun getAvailableRam(): Long = 0L
    fun getTotalSwap(): Long = 0L
    fun getFreeSwap(): Long = 0L
    fun getRam(column: String): Long = 0L
    fun refreshMemoryInfo()
}

interface TemperatureMonitor {
    fun getTemperature(slot: Int = 0): Float = Float.NaN
    fun getTemperatureCount(): Int
    fun refreshTemperatureInfo()
}

class SystemMonitor : CpuMonitor by MonitorFactory.createCpuMonitor(),
                      MemoryMonitor by MonitorFactory.createMemoryMonitor(),
                      TemperatureMonitor by MonitorFactory.createTemperatureMonitor() {

    fun refresh() {
        refreshCpuInfo()
        refreshMemoryInfo()
        refreshTemperatureInfo()
    }

}

fun LinuxCpuMonitorTest() {
    val cpuMonitor: (SystemMonitor) -> Unit = { monitor ->
        monitor.refreshCpuInfo()
        println("""
                    Cpu usage
                     * core: ${monitor.getCoreSize()}
                """.trimIndent())

        for (i in 0 until monitor.getCoreSize()) {
            val total = monitor.getTotalCpuUsage(i)
            val user = monitor.getUserCpuUsage(i)
            val system = monitor.getSystemCpuUsage(i)
            val free = monitor.getFreeCpuUsage(i)
            val avgTotal = monitor.getTotalCpuAvgUsage(i)
            val avgUser = monitor.getUserCpuAvgUsage(i)
            val avgSystem = monitor.getSystemCpuAvgUsage(i)
            val avgFree = monitor.getFreeCpuAvgUsage(i)

            if (i == 0) {
                println(" - CPU: total=$total(avg $avgTotal) user=$user(avg $avgUser) system=$system(avg $avgSystem) free=$free(avg $avgFree)")
            } else {
                println(" - CPU #${i}: total=$total(avg $avgTotal) user=$user(avg $avgUser) system=$system(avg $avgSystem) free=$free(avg $avgFree)")
            }
        }
    }

    val memoryMonitor: (SystemMonitor) -> Unit = { monitor ->
        monitor.refreshMemoryInfo()
        println("""
                    Memory usage
                     - total: ${monitor.getTotalRam().MB()} MB
                     - available: ${monitor.getAvailableRam().MB()} MB
                     - free: ${monitor.getFreeRam().MB()} MB
                    Swap usage
                     - total: ${monitor.getTotalSwap().MB()} MB
                     - free: ${monitor.getFreeSwap().MB()} MB
                """.trimIndent())
    }

    val temperatureMonitor: (SystemMonitor) -> Unit = { monitor ->
        monitor.refreshTemperatureInfo()
        println("""
                    Temperature
                     * sensors: ${monitor.getTemperatureCount()}
        """.trimIndent())

        for (i in 0 until monitor.getTemperatureCount()) {
            val temperature = monitor.getTemperature(i)
            println(" - Temperature #${i}: $temperature °C")
        }
    }

    SystemMonitor().let { monitor ->
        cpuMonitor(monitor)
        memoryMonitor(monitor)
        temperatureMonitor(monitor)
    }
}