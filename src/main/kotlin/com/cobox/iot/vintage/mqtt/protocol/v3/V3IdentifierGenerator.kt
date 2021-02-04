package com.cobox.iot.vintage.mqtt.protocol.v3

import com.cobox.iot.vintage.mqtt.protocol.IdentifierGenerator

object V3IdentifierGenerator: IdentifierGenerator {

    private var globalIdentifier = 1
    private val identifierLock = Any()

    override fun genIdentifier(): Int {
        synchronized(identifierLock) {
            return globalIdentifier++
        }
    }

}