package com.cobox.iot.vulture.system

import com.cobox.iot.vulture.system.linux.LinuxCpuMonitor
import com.cobox.iot.vulture.system.linux.LinuxMemoryMonitor
import com.cobox.iot.vulture.system.linux.LinuxTemperatureMonitor
import com.cobox.iot.vulture.system.macos.MacosCpuMonitor
import com.cobox.iot.vulture.system.macos.MacosMemoryMonitor
import com.cobox.iot.vulture.system.macos.MacosTemperatureMonitor
import com.cobox.iot.vulture.system.stub.StubCpuMonitor
import com.cobox.iot.vulture.system.stub.StubMemoryMonitor
import com.cobox.iot.vulture.system.stub.StubTemperatureMonitor
import com.cobox.iot.vulture.system.windows.WindowsCpuMonitor
import com.cobox.iot.vulture.system.windows.WindowsMemoryMonitor
import com.cobox.iot.vulture.system.windows.WindowsTemperatureMonitor
import com.cobox.utilites.system.OS

object MonitorFactory {
    fun createCpuMonitor(): CpuMonitor = when {
        OS.isLinux -> LinuxCpuMonitor()
        OS.isMacos -> MacosCpuMonitor()
        OS.isWindows -> WindowsCpuMonitor()
        else -> StubCpuMonitor()
    }

    fun createMemoryMonitor(): MemoryMonitor = when {
        OS.isLinux -> LinuxMemoryMonitor()
        OS.isMacos -> MacosMemoryMonitor()
        OS.isWindows -> WindowsMemoryMonitor()
        else -> StubMemoryMonitor()
    }

    fun createTemperatureMonitor(): TemperatureMonitor = when {
        OS.isLinux -> LinuxTemperatureMonitor()
        OS.isMacos -> MacosTemperatureMonitor()
        OS.isWindows -> WindowsTemperatureMonitor()
        else -> StubTemperatureMonitor()
    }
}