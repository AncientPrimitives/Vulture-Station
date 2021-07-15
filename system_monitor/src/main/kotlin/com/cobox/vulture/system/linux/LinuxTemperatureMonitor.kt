package com.cobox.vulture.system.linux

import com.cobox.vulture.system.TemperatureMonitor
import java.io.File
import java.io.FileReader

class LinuxTemperatureMonitor: TemperatureMonitor {

    private val temperatures = mutableMapOf<Int, Float>()

    override fun getTemperature(slot: Int): Float = temperatures[slot] ?: Float.NaN

    override fun getTemperatureCount(): Int = temperatures.size

    override fun refreshTemperatureInfo() {
        File("/sys/class/thermal/").let { parent ->
            if (!parent.exists()) return

            temperatures.clear()
            parent.list { _, name ->
                name.startsWith("thermal_zone")
            }?.forEach { temperatureInterfaces ->
                File(temperatureInterfaces).let { temperatureSource ->
                    val temperatureSourceIndex = temperatureSource.name.substringAfter("thermal_zone", "-1").toInt()
                    if (temperatureSourceIndex < 0) return@forEach

                    temperatures[temperatureSourceIndex] = runCatching {
                        FileReader(temperatureSource).use { reader ->
                            (reader.readText().trim().toInt() / 1000.0).toFloat()
                        }
                    }.getOrDefault(Float.NaN)
                }
            }
        }
    }
}