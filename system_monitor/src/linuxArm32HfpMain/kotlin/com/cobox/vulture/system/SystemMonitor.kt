package com.cobox.vulture.system

actual class SystemMonitor: CpuMonitor by LinuxCpuMonitor(),
                            MemoryMonitor by LinuxMemoryMonitor(),
                            TemperatureMonitor by LinuxTemperatureMonitor() {

    actual fun refresh() {
    }

}