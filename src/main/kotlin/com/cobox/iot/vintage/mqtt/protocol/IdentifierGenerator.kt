package com.cobox.iot.vintage.mqtt.protocol

interface IdentifierGenerator {

    companion object {
        public const val INVALID_ID = 0
    }

    fun genIdentifier(): Int
}