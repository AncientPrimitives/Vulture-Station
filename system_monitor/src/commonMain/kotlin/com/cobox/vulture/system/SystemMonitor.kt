package com.cobox.vulture.system

expect class SystemMonitor: CpuMonitor, MemoryMonitor, TemperatureMonitor {
    override fun getCpuCoreSize(): Int
    override fun getCpuTotalUsage(slot: Int): Float
    override fun getCpuUserUsage(slot: Int): Float
    override fun getCpuSystemUsage(slot: Int): Float
    override fun getCpuFreeUsage(slot: Int): Float
    override fun getCpuTotalAvgUsage(slot: Int): Float
    override fun getCpuUserAvgUsage(slot: Int): Float
    override fun getCpuSystemAvgUsage(slot: Int): Float
    override fun getCpuFreeAvgUsage(slot: Int): Float

    override fun getTotalMemUsage(): Long
    override fun getFreeMemUsage(): Long

    override fun getSystemTemperature(): Float

    fun refresh()
}