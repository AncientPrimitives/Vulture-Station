package com.cobox.iot.vintage.communicate

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageCodec

data class Event(val what: Int,
                 val payload: Any? = null) : MessageCodec<Event, Event> {

    private data class EventCodecState(val codec: Event, var isRegistered: Boolean)

    companion object {
        private val StaticEventCodec = EventCodecState(Event(-1), false)

        @Synchronized
        fun registerToEventBus(eventBus: EventBus) {
            if (!StaticEventCodec.isRegistered) {
                eventBus.registerDefaultCodec(StaticEventCodec.codec.javaClass, StaticEventCodec.codec)
                StaticEventCodec.isRegistered = true
            }
        }
    }

    val isValid = (what != -1)

    override fun name(): String = "com.cobox.iot.comm.Event"

    override fun systemCodecID(): Byte = -1

    override fun decodeFromWire(pos: Int, buffer: Buffer?): Event
            = if (buffer == null) {
                Event(-1)
            } else {
                Event(buffer.getInt(pos))
            }

    override fun encodeToWire(buffer: Buffer?, src: Event?) {
        if ((buffer == null) || (src == null)) {
            return
        }

        buffer.appendInt(src.what)
    }

    override fun transform(src: Event?): Event = Event(src?.what ?: -1, src?.payload)

}