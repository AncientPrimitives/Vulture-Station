package com.cobox.iot.vulture.iot

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.msgbus.Eventable
import io.vertx.core.eventbus.EventBus
import java.io.Closeable

class IotService(
    private val app: Application,
    private val database: IotDatabase
) : Iot, Closeable, Eventable {

    override fun onRegisterAddress(bus: EventBus) { }

    override fun createBusinessFor(username: String): Boolean = false

    override fun close() {
        println("[IOT] shutdown Iot service")
    }

}