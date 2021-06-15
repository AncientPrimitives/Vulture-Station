package com.cobox.utilites.log

import java.lang.management.ManagementFactory
import java.util.*

object Log {

    private enum class LogType(val text: String) {
        Debug("D"),
        Info("I"),
        Warn("W"),
        Error("E"),
        Verbose("V")
    }

    private val pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0].toIntOrNull()
    private val calender = Calendar.getInstance()

    private fun assemble(
        type: LogType, tag: String, msg: String, stacktrace: Throwable? = null
    ) = synchronized(calender) {
        /**
         * FMT: "2021-01-21 11:21:22.324 22311-22311 I/tag: msg"
         */
        calender.timeInMillis = System.currentTimeMillis()
        String.format(
            Locale.ENGLISH,
            "%04d-%02d-%02d %02d:%02d:%02d.%03d %d-%d %s/%s: %s",
            calender[Calendar.YEAR], calender[Calendar.MONTH] + 1, calender[Calendar.DAY_OF_MONTH],
            calender[Calendar.HOUR_OF_DAY], calender[Calendar.MINUTE], calender[Calendar.SECOND], calender[Calendar.MILLISECOND],
            pid ?: "??",
            pid?.let { it + Thread.currentThread().id - 1} ?: Thread.currentThread().id,
            type.text, tag, msg
        ).let { log ->
            when(type) {
                LogType.Info, LogType.Verbose, LogType.Debug -> System.out.println(log)
                LogType.Warn, LogType.Error -> System.err.println(log)
            }
        }
        stacktrace?.printStackTrace()
    }

    fun debug(tag: String, msg: String, stacktrace: Throwable? = null)
            = assemble(LogType.Debug, tag, msg, stacktrace)

    fun info(tag: String, msg: String, stacktrace: Throwable? = null)
            = assemble(LogType.Info, tag, msg, stacktrace)

    fun warn(tag: String, msg: String, stacktrace: Throwable? = null)
            = assemble(LogType.Warn, tag, msg, stacktrace)

    fun error(tag: String, msg: String, stacktrace: Throwable? = null)
            = assemble(LogType.Error, tag, msg, stacktrace)

    fun verbose(tag: String, msg: String, stacktrace: Throwable? = null)
            = assemble(LogType.Verbose, tag, msg, stacktrace)

}