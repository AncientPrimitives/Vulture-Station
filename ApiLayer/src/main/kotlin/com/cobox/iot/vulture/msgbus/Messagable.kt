package com.cobox.iot.vulture.msgbus

import io.vertx.core.eventbus.EventBus

interface Eventable {

    fun onRegisterAddress(bus: EventBus)

}