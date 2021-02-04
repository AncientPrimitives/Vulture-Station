package com.cobox.iot.vintage.devicecloud

import com.cobox.iot.vintage.mqtt.protocol.baseline.ConnackResponseCode
import com.cobox.iot.vintage.mqtt.protocol.baseline.ProtocolVersion
import com.cobox.iot.vintage.mqtt.protocol.baseline.QoS
import com.cobox.iot.vulture.application.Application
import io.netty.handler.codec.mqtt.MqttConnectReturnCode
import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.WorkerExecutor
import io.vertx.mqtt.MqttEndpoint
import io.vertx.mqtt.MqttServer
import io.vertx.mqtt.MqttServerOptions
import io.vertx.mqtt.messages.MqttPublishMessage
import io.vertx.mqtt.messages.MqttSubscribeMessage
import io.vertx.mqtt.messages.MqttUnsubscribeMessage

class DeviceCloud(
    val app: Application
) : AbstractVerticle() {

    companion object {
        const val SESSION_PRESENT = true
        const val SERVER_SUSPEND = false
        const val ENDPOINT_WORKER_SIZE = 4
        const val TASK_OREDERED = true
        const val TASK_UNOREDERED = false
    }

    private lateinit var server: MqttServer
    private lateinit var endpointWorkers: WorkerExecutor
    private val applicationPool: ApplicationPool = ApplicationPool.createFrom("")
    private val messager: DeviceCloudMessager = object : DeviceCloudMessager() {
        override fun onAskClientCount(): Int = applicationPool.sessionCount
    }

    override fun start(startFuture: Future<Void>?) {
        println("[MQTT] start MQTT server, vertx is $vertx")
        endpointWorkers = vertx.createSharedWorkerExecutor("EndPointWorkers", ENDPOINT_WORKER_SIZE)

        messager.start(vertx.eventBus())

        val options = MqttServerOptions()
        options.port = 1884 // 1883
        options.isSsl = false

        server = MqttServer.create(vertx, options)
        server.endpointHandler { newcome ->
            println("[MQTT] CONNECT: newcome(clientId:${newcome.clientIdentifier()}, username:'${newcome.auth().userName()}', password:'${newcome.auth().password()}', " +
                    "protocol:${newcome.protocolName()}_${ProtocolVersion.valueOf(newcome.protocolVersion())}, " +
                    "keepAlive:${newcome.keepAliveTimeSeconds() * 1000}ms, " +
                    "will:('${newcome.will().willTopic()}':'${newcome.will().willMessage()}':'${QoS.valueOf(newcome.will().willQos())}'))")

            endpointWorkers.executeBlocking<MqttEndpoint?> ({ future ->
                future.complete(onClientConnected(newcome))
            }, TASK_UNOREDERED, { result ->
                if (result.succeeded()) {
                    val endpoint = result.result()
                    if (endpoint != null) {
                        val session = createOrReplaceSession(applicationPool, endpoint)
                        setupEndpoint(session)
                    }
                }
            })

        }.listen { endpoint ->
            if (endpoint.succeeded()) {
                val server = endpoint.result()
                println("[MQTT] Device cloud is listening on ${server.actualPort()}")
                startFuture?.complete()
            } else {
                println("[MQTT] Device cloud cannot launched, caused by ${endpoint.cause().message}")
                startFuture?.fail(endpoint.cause())
            }
        }
    }

    private fun setupEndpoint(session: MqttSession) {
        val endpoint = session.endpoint
        endpoint.pingHandler { onClientPing(session) }
        endpoint.publishHandler { message -> onClientPublish(session, message) }
        endpoint.publishAcknowledgeHandler { messageId -> onClientPublishAcknowledged(session, messageId) }
        endpoint.publishReceivedHandler { messageId -> onClientPublishReceived(session, messageId) }
        endpoint.publishReleaseHandler { messageId -> onClientPublishReleased(session, messageId) }
        endpoint.publishCompleteHandler { messageId -> onClientPublishCompleted(session, messageId) }
        endpoint.subscribeHandler { message -> onClientSubscribe(session, message) }
        endpoint.unsubscribeHandler { message -> onClientUnsubscribe(session, message) }
        endpoint.disconnectHandler { onClientDisconnected(session) }
        endpoint.closeHandler { onClientClosed(session) }
    }

    private fun unsetupEndpoint(session: MqttSession) {
        val endpoint = session.endpoint
        endpoint.pingHandler(null)
        endpoint.publishHandler(null)
        endpoint.publishAcknowledgeHandler(null)
        endpoint.publishReceivedHandler(null)
        endpoint.publishReleaseHandler(null)
        endpoint.publishCompleteHandler(null)
        endpoint.subscribeHandler(null)
        endpoint.unsubscribeHandler(null)
        endpoint.disconnectHandler(null)
        endpoint.closeHandler(null)
    }

    private fun createOrReplaceSession(applicationPool: ApplicationPool, endpoint: MqttEndpoint): MqttSession {
        val existedSession = applicationPool.lookupSession(endpoint.clientIdentifier()) ?: return MqttSession(endpoint)

        val session: MqttSession
        if (endpoint.isCleanSession) {
            if (existedSession.endpoint.isConnected) {
                onServerDisconnecting(existedSession)
                existedSession.endpoint.close()
            }
            session = MqttSession(endpoint)
            applicationPool.registerSession(session)
        } else {
            session = existedSession
            session.endpoint = endpoint
        }
        return session
    }

    private fun onClientConnected(endpoint: MqttEndpoint): MqttEndpoint? {
        if (SERVER_SUSPEND) {
            endpoint.rejectWithLog(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE)
            return null
        }

        val protocolVersion = ProtocolVersion.valueOf(endpoint.protocolVersion())
        if ((protocolVersion != ProtocolVersion.V3_1_1) && (protocolVersion != ProtocolVersion.V5_0)) {
            endpoint.rejectWithLog(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION)
            return null
        }

        if (endpoint.clientIdentifier().isBlank()) {
            endpoint.rejectWithLog(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED)
            return null
        }

        if (endpoint.auth().userName().isNullOrBlank() || endpoint.auth().password().isNullOrBlank()) {
            endpoint.rejectWithLog(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD)
            return null
        } else if (!applicationPool.auth(endpoint.auth().userName(), endpoint.auth().password().toByteArray())) {
            endpoint.rejectWithLog(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED)
            return null
        } else {
            endpoint.acceptWithLog(SESSION_PRESENT)
        }

        return endpoint
    }

    private fun onClientPing(session: MqttSession) {
        println("[MQTT] PING: client ${session.endpoint.clientIdentifier()}")
        session.endpoint.pong()
    }

    private fun onClientPublish(session: MqttSession, message: MqttPublishMessage) {
        val endpoint = session.endpoint
        println("[MQTT] PUBLISH: client ${endpoint.clientIdentifier()} -> #${message.messageId()}:${message.topicName()}:${message.payload().length() / 1024f} kB")
        when (message.qosLevel()) {
            MqttQoS.AT_MOST_ONCE -> {
                /* no need acknowledge */
            }
            MqttQoS.AT_LEAST_ONCE -> {
                endpoint.publishAcknowledge(message.messageId())
            }
            MqttQoS.EXACTLY_ONCE -> {
                endpoint.publishReceived(message.messageId())
            }
            else -> {}
        }

//        dispatchMessageInAppGroup(message) {
//            sendToMessagePublishQueue(message)
//        }
    }

    private fun onClientPublishAcknowledged(session: MqttSession, messageId: Int) {
        val endpoint = session.endpoint
        println("[MQTT] PUBACK: #$messageId -> client ${endpoint.clientIdentifier()}")
    }

    private fun onClientPublishReceived(session: MqttSession, messageId: Int) {
        val endpoint = session.endpoint
        println("[MQTT] PUBREC: client ${endpoint.clientIdentifier()} for message #$messageId")
    }

    private fun onClientPublishReleased(session: MqttSession, messageId: Int) {
        val endpoint = session.endpoint
        println("[MQTT] PUBREL: client ${endpoint.clientIdentifier()} for message #$messageId")
//        when (session.findMesssage(messageId).message.qosLevel()) {
//            MqttQoS.EXACTLY_ONCE -> {
//                endpoint.publishComplete(message.messageId())
//            }
//            else -> {}
//        }
    }

    private fun onClientPublishCompleted(session: MqttSession, messageId: Int) {
        val endpoint = session.endpoint
        println("[MQTT] PUBCOMP: client ${endpoint.clientIdentifier()} for message #$messageId")
    }

    private fun onClientSubscribe(session: MqttSession, message: MqttSubscribeMessage) {
        val endpoint = session.endpoint
        for (topic in message.topicSubscriptions()) {
            println("[MQTT] SUBSCRIBE: client ${endpoint.clientIdentifier()} <- #${message.messageId()}:${topic.topicName()}")
        }
    }

    private fun onClientUnsubscribe(session: MqttSession, message: MqttUnsubscribeMessage) {
        val endpoint = session.endpoint
        for (topic in message.topics()) {
            println("[MQTT] UNSUBSCRIBE: client ${endpoint.clientIdentifier()} <- #${message.messageId()}:${topic}")
        }
    }

    private fun onServerDisconnecting(session: MqttSession) {
        val endpoint = session.endpoint
        println("[MQTT] SERVER DISCONNECTED: client (clientId:${endpoint.clientIdentifier()}, " +
                "will:('${endpoint.will().willTopic()}':'${endpoint.will().willMessage()}':'${QoS.valueOf(endpoint.will().willQos())}'))")
    }

    private fun onClientDisconnected(session: MqttSession) {
        val endpoint = session.endpoint
        println("[MQTT] CLIENT DISCONNECTED: client (clientId:${endpoint.clientIdentifier()}, " +
                "will:('${endpoint.will().willTopic()}':'${endpoint.will().willMessage()}':'${QoS.valueOf(endpoint.will().willQos())}'))")
    }

    private fun onClientClosed(session: MqttSession) {
        val endpoint = session.endpoint
        applicationPool.markAsDeactived(session)
        println("[MQTT] CLOSE: client (clientId:${endpoint.clientIdentifier()}, " +
                "will:('${endpoint.will().willTopic()}':'${endpoint.will().willMessage()}':'${QoS.valueOf(endpoint.will().willQos())}'))")
    }

    private fun MqttEndpoint.acceptWithLog(sessionPresent: Boolean): MqttEndpoint {
        println("[MQTT] CONNACK: ${ConnackResponseCode.ACCEPTED} for ${this.clientIdentifier()}")
        return this.accept(sessionPresent)
    }

    private fun MqttEndpoint.rejectWithLog(returnCode: MqttConnectReturnCode): MqttEndpoint {
        println("[MQTT] CONNACK: ${returnCode.convertToBaseline()} for ${this.clientIdentifier()}")
        return this.reject(returnCode)
    }

    fun MqttConnectReturnCode.convertToBaseline(): ConnackResponseCode = when (this) {
        MqttConnectReturnCode.CONNECTION_ACCEPTED -> ConnackResponseCode.ACCEPTED
        MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED -> ConnackResponseCode.NOT_AUTHORIZED
        MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE -> ConnackResponseCode.SERVER_UNAVAILABLE
        MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED -> ConnackResponseCode.IDENTIFIER_REJECTED
        MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD -> ConnackResponseCode.BAD_USERNAME_OR_PASSWORD
        MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION -> ConnackResponseCode.UNACCEPTABLE_PROTOCOL_VERSION
    }

}