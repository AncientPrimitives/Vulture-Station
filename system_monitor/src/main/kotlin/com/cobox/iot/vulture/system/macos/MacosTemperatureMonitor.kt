package com.cobox.iot.vulture.system.macos

import com.cobox.iot.vulture.system.TemperatureMonitor

class MacosTemperatureMonitor: TemperatureMonitor {
    override fun getTemperature(slot: Int): Float {
        return super.getTemperature(slot)
    }

    override fun getTemperatureCount(): Int {
        TODO("Not yet implemented")
    }

    override fun refreshTemperatureInfo() {
        TODO("Not yet implemented")
    }
}