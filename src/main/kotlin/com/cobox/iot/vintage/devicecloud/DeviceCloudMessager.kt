package com.cobox.iot.vintage.devicecloud

import com.cobox.iot.vintage.communicate.Event
import io.vertx.core.eventbus.EventBus

open class DeviceCloudMessager {

    companion object {
        public const val BUS_SERVER_STATE = "/mqtt/comm/server_state"
        public const val ASK_SESSION_COUNT = 0x1000
    }

    private lateinit var eventBus: EventBus

    fun start(bus: EventBus) {
        eventBus = bus
        Event.registerToEventBus(eventBus)
        setupMessageMap()
    }

    fun stop() {}

    private fun setupMessageMap() {
        eventBus.consumer<Event>(BUS_SERVER_STATE) { event ->
            when (event.body().what) {
                ASK_SESSION_COUNT -> {
                    event.reply(Event(ASK_SESSION_COUNT, onAskClientCount()))
                }
                else -> {

                }
            }
        }

    }

    open fun onAskClientCount() = 0

}