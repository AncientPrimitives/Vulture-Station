package com.cobox.iot.vintage.mqtt.protocol.baseline

object MqttFeature {
    const val canMaintainSession = true
}

/**
 * 控制报文类型
 */
enum class ControlPacketType(val type: Int) {

    /**
     * 无效
     */
    INVALID(0),

    /**
     * 客户端请求接入服务端
     */
    CONNECT(1),

    /**
     * 客户端接入服务端的请求应答
     */
    CONNACK(2),

    /**
     * 消息发布
     */
    PUBLISH(3),
    /**
     * 消息发布应答
     */
    PUBACK(4),

    /**
     * 发布的消息已接收
     */
    PUBREC(5),

    /**
     * 发布的消息已释放
     */
    PUBREL(6),

    /**
     * 发布的消息已完成
     */
    PUBCOMP(7),

    /**
     * 订阅消息请求
     */
    SUBSCRIBE(8),

    /**
     * 消息订阅应答
     */
    SUBACK(9),

    /**
     * 消息退订请求
     */
    UNSUBSCRIBE(10),

    /**
     * 消息退订应答
     */
    UNSUBACK(11),

    /**
     * 客户端像服务端发出心跳
     */
    PINGREQ(12),

    /**
     * 服务端应答客户端的心跳
     */
    PINGRESP(13),

    /**
     * 客户端请求与服务端断开连接
     */
    DISCONNECT(14);

    companion object {
        fun valueOf(type: Int): ControlPacketType {
            values().forEach { item ->
                if (item.type == type)
                    return item
            }
            return INVALID
        }
    }
}

/**
 * 传送质量
 */
enum class QoS(val type: Int) {
    /**
     * 无效
     */
    INVALID(-1),

    /**
     * 最多传送一次
     */
    AT_MOST_ONCE(0),

    /**
     * 至少传送一次
     */
    AT_LEAST_ONCE(1),

    /**
     * 仅传送一次
     */
    JUST_ONCE(2);

    companion object {
        fun valueOf(type: Int): QoS {
            values().forEach { item ->
                if (item.type == type)
                    return item
            }
            return INVALID
        }
    }

}

enum class ProtocolVersion(val version: Int) {
    INVALID(0),
    V3_1(3),
    V3_1_1(4),
    V5_0(5);

    companion object {
        fun valueOf(version: Int): ProtocolVersion {
            values().forEach { item ->
                if (item.version == version)
                    return item
            }
            return INVALID
        }
    }
}

/**
 * 连接返回码
 */
enum class ConnackResponseCode(val type: Int, val message: String) {
    INVALID(-1, "无效"),
    ACCEPTED(0, "接受连接请求"),
    UNACCEPTABLE_PROTOCOL_VERSION(1, "协议版本不支持"),
    IDENTIFIER_REJECTED(2, "非法唯一标识符"),
    SERVER_UNAVAILABLE(3, "服务器错误"),
    BAD_USERNAME_OR_PASSWORD(4, "用户名或密码错误"),
    NOT_AUTHORIZED(5, "身份认证不通过")
}