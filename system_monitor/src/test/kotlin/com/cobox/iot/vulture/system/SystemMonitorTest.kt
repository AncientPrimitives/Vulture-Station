package com.cobox.iot.vulture.system

import org.junit.jupiter.api.Test

class SystemMonitorTest {

    @Test
    fun cpuUsageTest() {
        SystemMonitor().let { monitor ->
            monitor.refresh()
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
                    println("CPU: total=$total(avg $avgTotal) user=$user(avg $avgUser) system=$system(avg $avgSystem) free=$free(avg $avgFree)")
                } else {
                    println("CPU #${i}: total=$total(avg $avgTotal) user=$user(avg $avgUser) system=$system(avg $avgSystem) free=$free(avg $avgFree)")
                }
            }

            assert(true)
        }
    }

}