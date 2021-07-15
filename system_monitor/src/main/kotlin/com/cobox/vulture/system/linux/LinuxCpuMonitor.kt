package com.cobox.vulture.system.linux

import com.cobox.vulture.system.CpuMonitor
import com.cobox.vulture.system.SystemMonitor
import com.cobox.utilites.log.Log
import com.cobox.vulture.standard.script.ShellReader
import java.nio.charset.Charset

class LinuxCpuMonitor: CpuMonitor {
    companion object {
        const val TAG = "LinuxCpuMonitor"
    }

    data class CpuJiffies(
        var user: Long = 0L,
        var nice: Long = 0L,
        var system: Long = 0L,
        var idle: Long = 0L,
        var iowait: Long = 0L,
        var irrq: Long = 0L,
        var softirq: Long = 0L,
        var steal: Long = 0L,
        var guest: Long = 0L,
        var guest_nice: Long = 0L
    )

    data class CpuUsageInfo (
        var name: String,
        var cpuIndex: Int, // 如为0，则记录的是总CPU占用率
        var userUsage: Float, // 用户进程CPU占比
        var systemUsage: Float, // 系统进程CPU占比
        var freeUsage: Float, // CPU空闲占比
        val details: CpuJiffies = CpuJiffies()
    )

    private val historicalAvgCpuUsages = mutableMapOf<Int, CpuUsageInfo>()
    private val avgCpuUsages = mutableMapOf<Int, CpuUsageInfo>()
    private val curCpuUsages = mutableMapOf<Int, CpuUsageInfo>()

    override fun getCoreSize(): Int = (curCpuUsages.size - 1).coerceAtLeast(0)

    override fun getTotalCpuUsage(slot: Int): Float =
        curCpuUsages[slot]?.let { usage ->
            usage.userUsage + usage.systemUsage
        } ?: 0.0f

    override fun getUserCpuUsage(slot: Int): Float =
        curCpuUsages[slot]?.userUsage ?: 0.0f

    override fun getSystemCpuUsage(slot: Int): Float =
        curCpuUsages[slot]?.systemUsage ?: 0.0f

    override fun getFreeCpuUsage(slot: Int): Float =
        curCpuUsages[slot]?.freeUsage ?: 0.0f

    override fun getTotalCpuAvgUsage(slot: Int): Float =
        avgCpuUsages[slot]?.let { usage ->
            usage.userUsage + usage.systemUsage
        } ?: 0.0f

    override fun getUserCpuAvgUsage(slot: Int): Float =
        avgCpuUsages[slot]?.userUsage ?: 0.0f

    override fun getSystemCpuAvgUsage(slot: Int): Float =
        avgCpuUsages[slot]?.systemUsage ?: 0.0f

    override fun getFreeCpuAvgUsage(slot: Int): Float =
        avgCpuUsages[slot]?.freeUsage ?: 0.0f

    override fun refreshCpuInfo() {
        // 获取的“/proc/stat”是开机后的平均值，瞬时值应该取两个次平均值后做差值
        // 1. 设立3个数据集previous, current, delta
        // 2. 如果首次运行
        //    2.1 读取“/proc/stat”，并更新到delta
        //    2.2 复制delta到previous, current
        //    2.3 以delta为瞬时值，以current为平均值
        // 3. 如果非首次运行
        //    3.1 复制current到previous
        //    3.2 读取“/proc/stat”，并更新到current
        //    3.2 以current为瞬时值，以previous为平均值
        val previous = historicalAvgCpuUsages
        val current = avgCpuUsages
        val delta = curCpuUsages
        val hasNoRefDataSet = current.isEmpty()

        if (hasNoRefDataSet) { // 首次运行
            queryCpuUsageInfo(delta)
            copyCpuUsageInfo(current, delta)
            copyCpuUsageInfo(previous, current)
        } else {
            copyCpuUsageInfo(previous, current)
            queryCpuUsageInfo(current)
            calcDeltaCpuUsageInfo(delta, current, previous)
        }
    }

    private fun calcDeltaCpuUsageInfo(
        delta: MutableMap<Int, CpuUsageInfo>,
        current: MutableMap<Int, CpuUsageInfo>,
        previous: MutableMap<Int, CpuUsageInfo>
    ) {
        delta.clear()
        if (previous.size != current.size) {
            Log.error(TAG, "[calcDeltaCpuUsageInfo] current and previous must be same dimension")
            return
        }

        for (i in 0 until current.size) {
            val alpha = current[i] ?: return
            val beta = previous[i] ?: return
            CpuUsageInfo(
                name = alpha.name,
                cpuIndex = alpha.cpuIndex,
                userUsage = alpha.userUsage - beta.userUsage,
                systemUsage = alpha.systemUsage - beta.systemUsage,
                freeUsage = alpha.freeUsage - beta.freeUsage,
                details = CpuJiffies(
                    user = alpha.details.user - beta.details.user,
                    nice = alpha.details.nice - beta.details.nice,
                    system = alpha.details.system - beta.details.system,
                    idle = alpha.details.idle - beta.details.idle,
                    iowait = alpha.details.iowait - beta.details.iowait,
                    irrq = alpha.details.irrq - beta.details.irrq,
                    softirq = alpha.details.softirq - beta.details.softirq,
                    steal = alpha.details.steal - beta.details.steal,
                    guest = alpha.details.guest - beta.details.guest,
                    guest_nice = alpha.details.guest_nice - beta.details.guest_nice
                )
            ).let { diff ->
                delta[diff.cpuIndex] = diff
            }
        }
    }

    private fun copyCpuUsageInfo(
        dest: MutableMap<Int, CpuUsageInfo>,
        src: MutableMap<Int, CpuUsageInfo>
    ) {
        dest.clear()
        src.values.forEach { info ->
            dest[info.cpuIndex] = info
        }
    }

    private fun queryCpuUsageInfo(usageInfo: MutableMap<Int, CpuUsageInfo>) {
        ShellReader("/proc/stat", Charset.forName("utf-8")).use { reader ->
            while (true) {
                reader.readLine()?.let { line ->
                    if (!line.startsWith("cpu")) return@let
                    line.split("\\s+".toRegex()).let { columns ->
                        kotlin.runCatching {
                            val name: String = columns[0].trim()
                            val user: Long = columns[1].trim().toLong()
                            val nice: Long = columns[2].trim().toLong()
                            val system: Long = columns[3].trim().toLong()
                            val idle: Long = columns[4].trim().toLong()
                            val iowait: Long = columns[5].trim().toLong()
                            val irrq: Long = columns[6].trim().toLong()
                            val softirq: Long = columns[7].trim().toLong()
                            val steal: Long = columns[8].trim().toLong()
                            val guest: Long = columns[9].trim().toLong()
                            val guest_nice: Long = columns[10].trim().toLong()

                            val cpuIndex = name.split("cpu").let { nameColumns ->
                                if (nameColumns.size < 2) {
                                    0
                                } else {
                                    if (nameColumns[1].trim().isEmpty()) 0 else (nameColumns[1].toInt() + 1)
                                }
                            }
                            val totalJiffies = user + nice + system + idle + iowait + irrq
                            + softirq + steal + guest + guest_nice

                            CpuUsageInfo(
                                name = name,
                                cpuIndex = cpuIndex,
                                userUsage = (user.toDouble() / totalJiffies.toDouble()).toFloat(),
                                systemUsage = (system.toDouble() / totalJiffies.toDouble()).toFloat(),
                                freeUsage = (idle.toDouble() / totalJiffies.toDouble()).toFloat(),
                                details = CpuJiffies(
                                    user, nice, system, idle, iowait, irrq, softirq, steal, guest, guest_nice
                                )
                            ).let { info ->
                                usageInfo.put(cpuIndex, info)
                            }
                        }.onFailure {
                            Log.error(TAG, "[queryCpuUsageInfo]", it)
                        }
                    }
                } ?: return@use
            }
        }
    }
}