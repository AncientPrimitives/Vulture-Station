package com.cobox.vulture.system.stub

import com.cobox.vulture.system.TemperatureMonitor

class StubTemperatureMonitor: TemperatureMonitor {
    override fun getTemperature(slot: Int): Float = Float.NaN
    override fun getTemperatureCount(): Int = 0
    override fun refreshTemperatureInfo() { }
}