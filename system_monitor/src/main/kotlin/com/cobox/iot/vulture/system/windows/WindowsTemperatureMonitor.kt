package com.cobox.iot.vulture.system.windows

import com.cobox.iot.vulture.system.TemperatureMonitor

class WindowsTemperatureMonitor: TemperatureMonitor {
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