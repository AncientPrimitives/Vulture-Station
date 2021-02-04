package com.cobox.iot.vintage.mqtt.protocol.v3

class Config {

    companion object {
        /**
         * UTF-8编码的字符串中的字符必须是Unicode规范里定义的并在RFC 3629里重申的符合规范的UTF-8。
         * U+0001..U+001F控制字符、U+007F..U+009F控制字符、Unicode规范定义的非字符代码（例如U+0FFFF），
         * 尤其是这些数据不能包含介于U+D800到U+DFFF之间的编码。如果服务端或客户端收到了控制包包含不合规的UTF编码，
         * 就必须关闭网络连接[MQTT-1.5.3-1]。
         */
        const val shouldDisconnectIfStringContainsInvalidCode = false

        /**
         * UTF-8编码字符串不能包含空字符U+0000。如果收到控制包包含U+0000，服务端或客户端必须关闭网络连接[MQTT-1.5.3-2]。
         */
        const val shouldDisconnectIfStringContainsEmptyCode = false
    }

}