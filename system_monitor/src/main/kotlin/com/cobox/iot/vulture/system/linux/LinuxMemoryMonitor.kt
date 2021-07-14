package com.cobox.iot.vulture.system.linux

import com.cobox.iot.vulture.system.MemoryMonitor
import com.cobox.utilites.log.Log
import com.cobox.utilites.unit.StorageUnit
import com.cobox.utilites.unit.toStorageUnit
import com.cobox.vulture.standard.script.ShellReader

/**
 * MemTotal:          45964 kB    // 所有可用的内存大小，物理内存减去预留位和内核使用。系统从加电开始到引导完成，firmware/BIOS要预留一些内存，内核本身要占用一些内存，最后剩下可供内核支配的内存就是MemTotal。这个值在系统运行期间一般是固定不变的，重启会改变。
 * MemFree:            1636 kB    // 表示系统尚未使用的内存。
 * MemAvailable:       8496 kB    // 真正的系统可用内存，系统中有些内存虽然已被使用但是可以回收的，比如cache/buffer、slab都有一部分可以回收，所以这部分可回收的内存加上MemFree才是系统可用的内存
 * Buffers:               0 kB    // 用来给块设备做缓存的内存，(文件系统的 metadata、pages)
 * Cached:             7828 kB    // 分配给文件缓冲区的内存,例如vi一个文件，就会将未保存的内容写到该缓冲区
 * SwapCached:            0 kB    // 被高速缓冲存储用的交换空间（硬盘的swap）的大小
 * Active:            19772 kB    // 经常使用的高速缓冲存储器页面文件大小
 * Inactive:           3128 kB    // 不经常使用的高速缓冲存储器文件大小
 * Active(anon):      15124 kB    // 活跃的匿名内存
 * Inactive(anon):       52 kB    // 不活跃的匿名内存
 * Active(file):       4648 kB    // 活跃的文件使用内存
 * Inactive(file):     3076 kB    // 不活跃的文件使用内存
 * Unevictable:           0 kB    // 不能被释放的内存页
 * Mlocked:               0 kB    // 系统调用 mlock 家族允许程序在物理内存上锁住它的部分或全部地址空间。这将阻止Linux 将这个内存页调度到交换空间（swap space），即使该程序已有一段时间没有访问这段空间
 * SwapTotal:             0 kB    // 交换空间总内存
 * SwapFree:              0 kB    // 交换空间空闲内存
 * Dirty:                 4 kB    // 等待被写回到磁盘的
 * Writeback:             0 kB    // 正在被写回的
 * AnonPages:         15100 kB    // 未映射页的内存/映射到用户空间的非文件页表大小
 * Mapped:             7160 kB    // 映射文件内存
 * Shmem:               100 kB    // 已经被分配的共享内存
 * Slab:               9236 kB    // 内核数据结构缓存
 * SReclaimable:       2316 kB    // 可收回slab内存
 * SUnreclaim:         6920 kB    // 不可收回slab内存
 * KernelStack:        2408 kB    // 内核消耗的内存
 * PageTables:         1268 kB    // 管理内存分页的索引表的大小
 * NFS_Unstable:          0 kB    // 不稳定页表的大小
 * Bounce:                0 kB    // 在低端内存中分配一个临时buffer作为跳转，把位于高端内存的缓存数据复制到此处消耗的内存
 * WritebackTmp:          0 kB    // FUSE用于临时写回缓冲区的内存
 * CommitLimit:       22980 kB    // 系统实际可分配内存
 * Committed_AS:     536244 kB    // 系统当前已分配的内存
 * VmallocTotal:     892928 kB    // 预留的虚拟内存总量
 * VmallocUsed:       29064 kB    // 已经被使用的虚拟内存
 * VmallocChunk:     860156 kB    // 可分配的最大的逻辑连续的虚拟内存
 */
class LinuxMemoryMonitor: MemoryMonitor {
    companion object {
        const val TAG = "LinuxMemoryMonitor"
    }

    enum class MemoryColumn {
        MemTotal, MemFree, MemAvailable,
        Buffers, Cached, SwapCached,
        Active, Inactive,
        AnonActive, AnonInactive,
        FileActive, FileInactive,
        Unevictable, Mlocked,
        SwapTotal, SwapFree,
        Dirty, Writeback, AnonPages,
        Mapped, Shmem, KReclaimable,
        Slab, SReclaimable, SUnreclaim,
        KernelStack, PageTables, NFS_Unstable,
        Bounce, WritebackTmp,
        CommitLimit, Committed_AS,
        VmallocTotal, VmallocUsed, VmallocChunk,
        Percpu, HardwareCorrupted,
        AnonHugePages, ShmemHugePages, ShmemPmdMapped,
        FileHugePages, FilePmdMapped,
        CmaTotal, CmaFree,
        HugePages_Total, HugePages_Free, HugePages_Rsvd, HugePages_Surp,
        Hugepagesize, Hugetlb,
        DirectMap4k, DirectMap2M, DirectMap1G,
        Unknown;

        val columnName: String
            get() = when(this) {
                AnonActive -> "Active(anon)"
                AnonInactive -> "Inactive(anon)"
                FileActive -> "Active(file)"
                FileInactive -> "Inactive(file)"
                else -> this.name
            }

        companion object {
            fun convertToMemoryColumn(name: String): MemoryColumn =
                when(name.toLowerCase()) {
                    "active(anon)" -> AnonActive
                    "inactive(anon)" -> AnonInactive
                    "active(file)" -> FileActive
                    "Inactive(file)" -> FileInactive
                    else -> {
                        var result: MemoryColumn = Unknown
                        values().forEach {
                            if (it.columnName == name) {
                                result = it
                                return@forEach
                            }
                        }
                        result
                    }
                }
        }
    }

    private val usages: MutableMap<String, Long> = mutableMapOf()

    override fun getTotalRam(): Long = usages[MemoryColumn.MemTotal.columnName] ?: 0L

    override fun getFreeRam(): Long = usages[MemoryColumn.MemFree.columnName] ?: 0L

    override fun getAvailableRam(): Long = usages[MemoryColumn.MemAvailable.columnName] ?: 0L

    override fun getTotalSwap(): Long = usages[MemoryColumn.SwapTotal.columnName] ?: 0L

    override fun getFreeSwap(): Long = usages[MemoryColumn.SwapFree.columnName] ?: 0L

    override fun getRam(column: String): Long = usages[MemoryColumn.convertToMemoryColumn(column).columnName] ?: 0L

    override fun refreshMemoryInfo() {
        ShellReader("/proc/meminfo", Charsets.UTF_8).use { reader ->
            while (true) {
                reader.readLine()?.let { line ->
                    line.split("\\s+".toRegex()).let { columns ->
                        kotlin.runCatching {
                            if (columns.size < 2) return@let

                            val column = columns[0].let { it.substringBefore(":", it) }
                            val value = columns[1].toLong()
                            val unit = if (columns.size >= 3) columns[2].toStorageUnit() else StorageUnit.Byte
                            usages[MemoryColumn.convertToMemoryColumn(column).columnName] = unit.calcBytes(value)
                        }.onFailure {
                            Log.error(TAG, "[refreshMemoryInfo]", it)
                        }
                    }
                } ?: return@use
            }
        }
    }
}