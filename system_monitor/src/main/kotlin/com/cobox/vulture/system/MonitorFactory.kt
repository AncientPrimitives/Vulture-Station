package com.cobox.vulture.system

import com.cobox.vulture.system.linux.LinuxCpuMonitor
import com.cobox.vulture.system.linux.LinuxMemoryMonitor
import com.cobox.vulture.system.linux.LinuxTemperatureMonitor
import com.cobox.vulture.system.macos.MacosCpuMonitor
import com.cobox.vulture.system.macos.MacosMemoryMonitor
import com.cobox.vulture.system.macos.MacosTemperatureMonitor
import com.cobox.vulture.system.stub.StubCpuMonitor
import com.cobox.vulture.system.stub.StubMemoryMonitor
import com.cobox.vulture.system.stub.StubTemperatureMonitor
import com.cobox.vulture.system.windows.WindowsCpuMonitor
import com.cobox.vulture.system.windows.WindowsMemoryMonitor
import com.cobox.vulture.system.windows.WindowsTemperatureMonitor
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