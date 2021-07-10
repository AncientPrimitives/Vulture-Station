package com.cobox.iot.vulture.system

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
    fun refreshMemoryInfo()
}

interface TemperatureMonitor {
    fun getTemperature(): Float = 0.0f
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