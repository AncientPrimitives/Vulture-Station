package com.cobox.vulture.system

interface MemoryMonitor {
    fun getTotalMemUsage(): Long
    fun getFreeMemUsage(): Long
}