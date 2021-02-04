package com.cobox.iot.vintage.devicecloud

import io.netty.handler.codec.mqtt.MqttConnectReturnCode
import io.netty.handler.codec.mqtt.MqttQoS
import io.netty.handler.codec.mqtt.MqttVersion
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.mqtt.MqttAuth
import io.vertx.mqtt.MqttEndpoint
import io.vertx.mqtt.MqttWill
import io.vertx.mqtt.messages.MqttPublishMessage
import io.vertx.mqtt.messages.MqttSubscribeMessage
import io.vertx.mqtt.messages.MqttUnsubscribeMessage

class MqttSession internal constructor(var endpoint: MqttEndpoint) {

    private val dummyEndpoint = EmptyEndpoint()

    val clientId: String
        get() = endpoint.clientIdentifier()

    fun disposeEndpoint() {
        endpoint = dummyEndpoint
    }

    private class EmptyEndpoint : MqttEndpoint {

        private val emptyAuth = object : MqttAuth {
            override fun password(): String = ""
            override fun userName(): String = ""
        }

        private val emptyWill = object : MqttWill {
            override fun willMessage(): String = ""
            override fun willQos(): Int = MqttQoS.FAILURE.value()
            override fun isWillRetain(): Boolean = false
            override fun isWillFlag(): Boolean = false
            override fun willTopic(): String = ""
        }

        override fun isConnected(): Boolean = false
        override fun will(): MqttWill = emptyWill
        override fun publishComplete(publishMessageId: Int): MqttEndpoint = this
        override fun auth(): MqttAuth = emptyAuth
        override fun pong(): MqttEndpoint = this
        override fun clientIdentifier(): String = ""
        override fun lastMessageId(): Int = 0
        override fun reject(returnCode: MqttConnectReturnCode?): MqttEndpoint = this
        override fun subscriptionAutoAck(isSubscriptionAutoAck: Boolean) {}
        override fun isCleanSession(): Boolean = true
        override fun protocolVersion(): Int = MqttVersion.MQTT_3_1_1.protocolLevel().toInt()
        override fun protocolName(): String = MqttVersion.MQTT_3_1_1.protocolName()
        override fun keepAliveTimeSeconds(): Int = 0
        override fun setClientIdentifier(clientIdentifier: String?): MqttEndpoint = this
        override fun isPublishAutoAck(): Boolean = false
        override fun unsubscribeAcknowledge(unsubscribeMessageId: Int): MqttEndpoint = this
        override fun autoKeepAlive(isAutoKeepAlive: Boolean): MqttEndpoint = this
        override fun isAutoKeepAlive(): Boolean = false
        override fun publishAcknowledge(publishMessageId: Int): MqttEndpoint = this
        override fun publishReceived(publishMessageId: Int): MqttEndpoint = this
        override fun publishAutoAck(isPublishAutoAck: Boolean): MqttEndpoint = this
        override fun isSubscriptionAutoAck(): Boolean = false
        override fun accept(sessionPresent: Boolean): MqttEndpoint = this
        override fun close() {}
        override fun publishRelease(publishMessageId: Int): MqttEndpoint = this
        override fun subscribeAcknowledge(subscribeMessageId: Int, grantedQoSLevels: MutableList<MqttQoS>?): MqttEndpoint = this
        override fun publish(topic: String?, payload: Buffer?, qosLevel: MqttQoS?, isDup: Boolean, isRetain: Boolean): MqttEndpoint = this

        override fun subscribeHandler(handler: Handler<MqttSubscribeMessage>?): MqttEndpoint = this
        override fun publishReleaseHandler(handler: Handler<Int>?): MqttEndpoint = this
        override fun disconnectHandler(handler: Handler<Void>?): MqttEndpoint = this
        override fun pingHandler(handler: Handler<Void>?): MqttEndpoint = this
        override fun publishAcknowledgeHandler(handler: Handler<Int>?): MqttEndpoint = this
        override fun publishHandler(handler: Handler<MqttPublishMessage>?): MqttEndpoint = this
        override fun publishReceivedHandler(handler: Handler<Int>?): MqttEndpoint = this
        override fun publishCompleteHandler(handler: Handler<Int>?): MqttEndpoint = this
        override fun unsubscribeHandler(handler: Handler<MqttUnsubscribeMessage>?): MqttEndpoint = this
        override fun closeHandler(handler: Handler<Void>?): MqttEndpoint = this
    }


}