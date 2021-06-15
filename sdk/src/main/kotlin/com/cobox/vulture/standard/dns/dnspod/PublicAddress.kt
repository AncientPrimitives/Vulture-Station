package com.cobox.vulture.standard.dns.dnspod

import com.cobox.utilites.log.Log
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.NetSocket
import java.net.InetAddress

object PublicAddress {

    const val TAG = "DnsPod.PublicAddress"

    const val NS_HOST = "ns1.dnspod.net"
    const val NS_PORT = 6666

    const val NS_RETRY_COUNT = 3
    const val NS_RETRY_DELAY = 3 * 1000L // 3s
    const val NS_CONNECT_TIMEOUT = 30 * 1000 // 30s

    /**
     * 查询自己的公网IP
     */
    fun Vertx.queryPublicIP(
        nsHost: String = NS_HOST,
        nsPort: Int = NS_PORT,
        queryTimeout: Int = NS_CONNECT_TIMEOUT,
        failedRetryCount: Int = NS_RETRY_COUNT,
        failedRetryInterval: Long = NS_RETRY_DELAY,
        callback: ((InetAddress?) -> Unit)
    ) {
        createNetClient(
            NetClientOptions().apply {
                connectTimeout = queryTimeout
                reconnectAttempts = failedRetryCount
                reconnectInterval = failedRetryInterval
            }
        ).let { client ->
            client.connect(nsPort, nsHost) { result: AsyncResult<NetSocket> ->
                if (result.failed()) {
                    Log.warn(TAG, "[queryPublicIP] query public ip failed, caused by ${result.cause()}", result.cause())
                    callback(null)
                } else {
                    result.result().handler { buffer ->
                        val publicIp = String(buffer.bytes)
                        Log.info(TAG, "[queryPublicIP] requested public ip from NS: $publicIp")
                        callback(InetAddress.getByName(publicIp))
                    }
                }
            }
        }
    }

}