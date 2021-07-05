package com.cobox.vulture.system

interface CpuMonitor {
    fun getCpuCoreSize(): Int
    fun getCpuTotalUsage(slot: Int = 0): Float
    fun getCpuUserUsage(slot: Int = 0): Float
    fun getCpuSystemUsage(slot: Int = 0): Float
    fun getCpuFreeUsage(slot: Int = 0): Float
    fun getCpuTotalAvgUsage(slot: Int = 0): Float
    fun getCpuUserAvgUsage(slot: Int = 0): Float
    fun getCpuSystemAvgUsage(slot: Int = 0): Float
    fun getCpuFreeAvgUsage(slot: Int = 0): Float
}