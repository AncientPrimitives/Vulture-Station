package com.cobox.iot.vintage.mqtt.protocol.v3

import com.cobox.iot.vintage.mqtt.protocol.baseline.QoS
import io.vertx.core.buffer.Buffer
import kotlin.math.pow

internal object BaselineCodec {

    /**
     * Table 2.2 -Flag Bits
     * |Control Package    |Fixed header flags     |bit3           |bit2           |bit1           |bit0
     * |CONNECT            |Reserved               |0              |0              |0              |0
     * |CONNACK            |Reserved               |0              |0              |0              |0
     * |PUBLISH            |Used in MQTT 3.1.1     |DUP1           |QoS2           |QoS2           |RETAIN3
     * |PUBACK             |Reserved               |0              |0              |0              |0
     * |PUBREC             |Reserved               |0              |0              |0              |0
     * |PUBREL             |Reserved               |0              |0              |1              |0
     * |PUBCOMP            |Reserved               |0              |0              |0              |0
     * |SUBSCRIBE          |Reserved               |0              |0              |1              |0
     * |SUBACK             |Reserved               |0              |0              |0              |0
     * |UNSUBSCRIBE        |Reserved               |0              |0              |1              |0
     * |UNSUBACK           |Reserved               |0              |0              |0              |0
     * |PINGREQ            |Reserved               |0              |0              |0              |0
     * |PINGRESP           |Reserved               |0              |0              |0              |0
     * |DISCONNECT         |Reserved               |0              |0              |0              |0
     */


    /**
     * 生成固定报头中的4字节“内容长度”整数
     */
    fun generateContentLength(length: Long) : ByteArray {
        var position = 0

        val buffer = ByteArray(4) // TODO("内存碎片")
        var source = length
        do {
            var current = source % 0b1000_0000
            source /= 0b1000_0000
            if (source > 0) {
                current = current or 0b1000_0000
            }
            buffer[position] = current.toByte()
            position++
        } while (source > 0)

        return buffer.copyOf(position)
    }

    /**
     * 解读固定报头中的4字节“内容长度”整数
     */
    fun readContentLength(stream: Buffer, offset: Int,
                          callback: (readBytes: Int, contentLength: Long) -> Unit) : Int {
        var readBytes: Int = 0
        var result: Long = 0

        for (index in 0..3) {
            val byte = stream.getByte(offset + index)
            val hasNextByte = (byte.toInt() and 0b1000_0000) != 0
            result += (byte.toInt() and 0b0111_1111) * 128.0.pow(index.toDouble()).toLong()
            readBytes++

            if (!hasNextByte) {
                break
            }
        }

        callback(readBytes, result)
        return readBytes
    }

    /**
     * Figure 1.1 Structure of UTF-8 encoded strings
     * |bit     |7       |6       |5       |4       |3       |2       |1       |0
     * |byte 1  |string length MSB
     * |byte 2  |string length LSB
     * |byte 3  |UTF-8 Encoded CharacterData, if length > 0.
     */
    fun readString(stream: Buffer, offset: Int,
                   callback: (readBytes: Int, text: String) -> Unit): Int {
        val length: Int = stream.getUnsignedShort(offset)
        val text = stream.getString(offset + 2, offset + 2 + length, Charsets.UTF_8.name())
        val readBytes = 2 + length
        callback(readBytes, text)
        return readBytes
    }

    /**
     * 读取MQTT协议版本号
     */
    fun readProtocolVersion(stream: Buffer, offset: Int,
                            callback: (readBytes: Int, versionCode: Int) -> Unit): Int {
        val version = stream.getByte(offset).toInt()
        callback(1, version)
        return 1
    }

    /**
     * 读取CONNECT的Flags
     * Figure 3.4 - Connect Flag bits
     *
     * |Bit        |7                |6              |5              |4  |3       |2          |1              |0
     * |           |User Name Flag   |Password Flag  |Will Retain    |Will QoS    |Will Flag  |Clean Session  |Reserved
     * |byte 8     |X                |X              |X              |X  |X       |X          |X              |0
     */
    fun readConnectFlags(stream: Buffer, offset: Int,
                         callback: (readBytes: Int,
                                    hasUserName: Boolean, hasPassword: Boolean,
                                    shouldRetainWill: Boolean, willQoS: QoS, hasWill: Boolean,
                                    shouldMaintainSession: Boolean,
                                    reserved: Int) -> Unit): Int {
        val raw = stream.getByte(offset).toInt()
        val hasUserName = (raw and 0b1000_0000) != 0
        val hasPassword = (raw and 0b0100_0000) != 0
        val shouldRetainWill = (raw and 0b0010_0000) != 0
        val willQoS = (raw and 0b0001_1000).shr(3)
        val hasWill = (raw and 0b0000_0100) != 0
        val shouldMaintainSession = (raw and 0b0000_0010) != 0
        val reserved = (raw and 0b0000_0001)
        callback(1, hasUserName, hasPassword,
            shouldRetainWill, QoS.valueOf(willQoS), hasWill,
            shouldMaintainSession, reserved)
        return 1
    }

    /**
     * 读取保持连接的时长
     */
    fun readKeepAlive(stream: Buffer, offset: Int,
                      callback: (readBytes: Int, keepAlive: Long) -> Unit): Int {
        val keepAlive = stream.getUnsignedShort(offset)
        callback(2, keepAlive * 1000L)
        return 2
    }

    /**
     * 读取客户端标识
     */
    fun readClientId(stream: Buffer, offset: Int,
                     callback: (readBytes: Int, clientId: String) -> Unit): Int
            = readString(stream, offset, callback)

    /**
     * 读取遗愿话题
     */
    fun readWillTopic(stream: Buffer, offset: Int,
                      callback: (readBytes: Int, willTopic: String) -> Unit): Int
            = readString(stream, offset, callback)

    /**
     * 读取遗愿内容
     */
    fun readWillMessage(stream: Buffer, offset: Int,
                        callback: (readBytes: Int, willMessage: String) -> Unit): Int
            = readString(stream, offset, callback)

    /**
     * 读取认证用户名
     */
    fun readUserName(stream: Buffer, offset: Int,
                     callback: (readBytes: Int, userName: String) -> Unit): Int
            = readString(stream, offset, callback)

    /**
     * 读取认证密码
     */
    fun readPassword(stream: Buffer, offset: Int,
                     callback: (readBytes: Int, password: ByteArray) -> Unit): Int {
        val length = stream.getUnsignedShort(offset)
        val password = stream.getBytes(offset + 2, offset + 2 + length)
        val readBytes = 2 + length
        callback(readBytes, password)
        return readBytes
    }

}