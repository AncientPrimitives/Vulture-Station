package com.cobox.iot.vulture.system.stub

import com.cobox.iot.vulture.system.TemperatureMonitor

class StubTemperatureMonitor: TemperatureMonitor {
    override fun getTemperature(slot: Int): Float = Float.NaN
    override fun getTemperatureCount(): Int = 0
    override fun refreshTemperatureInfo() { }
}