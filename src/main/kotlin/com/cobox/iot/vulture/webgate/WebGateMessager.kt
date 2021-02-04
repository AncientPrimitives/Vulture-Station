package com.cobox.iot.vulture.webgate

import com.cobox.iot.vintage.communicate.Event
import com.cobox.iot.vintage.devicecloud.DeviceCloudMessager
import com.cobox.iot.vulture.maintain.MaintianerMessager
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message

class WebGateMessager {

    private lateinit var eventBus: EventBus

    fun start(bus: EventBus) {
        eventBus = bus
        Event.registerToEventBus(eventBus)
    }

    fun stop() {}

    fun askMqttServer_SessionCount(callback: (count: Int) -> Unit) {
        eventBus.send(
            DeviceCloudMessager.BUS_SERVER_STATE,
            Event(DeviceCloudMessager.ASK_SESSION_COUNT)) { event: AsyncResult<Message<Event>> ->
                if (!event.failed()) {
                    val count = event.result().body().payload as Int
                    callback(count)
                } else {
                    callback(-1)
                }
        }
    }

    fun askMaintainer_IpConfig(
        callback: (
            publicNetworkAddress: String,
            dnsRecord: String
        ) -> Unit
    ) {
        eventBus.send(
            MaintianerMessager.IP_CONFIG,
            Event(MaintianerMessager.ASK_IP_CONFIG)) { event: AsyncResult<Message<Event>> ->
            if (!event.failed()) {
                val event = event.result().body()
                val payload = (event.payload ?: "0.0.0.0|0.0.0.0").toString()
                val ipConfigs = payload.split("|")
                val dnsRecord = ipConfigs[0]
                val publicNetworkAddress = ipConfigs[1]
                callback(publicNetworkAddress, dnsRecord)
            } else {
                callback("0.0.0.0", "0.0.0.0")
            }
        }
    }

}