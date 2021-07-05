package com.cobox.vulture.system

actual class SystemMonitor: CpuMonitor, MemoryMonitor, TemperatureMonitor {
    actual override fun getCpuCoreSize(): Int {
        TODO("Not yet implemented")
    }

    actual override fun getCpuTotalUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuUserUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuSystemUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuFreeUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuTotalAvgUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuUserAvgUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuSystemAvgUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getCpuFreeAvgUsage(slot: Int): Float {
        TODO("Not yet implemented")
    }

    actual override fun getTotalMemUsage(): Long {
        TODO("Not yet implemented")
    }

    actual override fun getFreeMemUsage(): Long {
        TODO("Not yet implemented")
    }

    actual override fun getSystemTemperature(): Float {
        TODO("Not yet implemented")
    }

    actual fun refresh() {
    }
}