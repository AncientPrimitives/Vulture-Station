package com.cobox.iot.vulture.maintain

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.companion.QCloud
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import kotlin.random.Random

class Maintainer(
    val app: Application
) : AbstractVerticle() {

    companion object {
        const val HOST_DNSPOD = "119.29.29.29"
        const val LOCAL_DO_NAME = "api.cocoonshu.com"
        const val HOST_QCLOUD = "https://cns.api.qcloud.com/v2/index.php"
    }

    private lateinit var httpClient: HttpClient
    private val messager: MaintianerMessager = object : MaintianerMessager() {

        override fun onAskIpConfig(future: Future<String>) {
            val query = "/d?dn=$LOCAL_DO_NAME&clientip=1"
            httpClient.get(HOST_DNSPOD, query) { response: HttpClientResponse? ->
                response?.bodyHandler { buffer: Buffer? ->
                    if (buffer == null) {
                        future.fail(response.statusMessage())
                    } else {
                        /**
                         * 返回结果："0.0.0.0|0.0.0.0"
                         * 其中split("|")[0]为DNS的记录
                         *    split("|")[1]为请求者的公网IP
                         */
                        val result = buffer.getString(0, buffer.length(), Charsets.UTF_8.name()) ?: "0.0.0.0|0.0.0.0"
                        future.complete(result)
                    }
                }
            }.end()
        }

        override fun onAskPublicNetworkAddress(future: Future<String>) {
            val query = "/d?dn=$LOCAL_DO_NAME&clientip=1"
            httpClient.get(HOST_DNSPOD, query) { response: HttpClientResponse? ->
                response?.bodyHandler { buffer: Buffer? ->
                    if (buffer == null) {
                        future.fail(response.statusMessage())
                    } else {
                        /**
                         * 返回结果："0.0.0.0|0.0.0.0"
                         * 其中split("|")[0]为DNS的记录
                         *    split("|")[1]为请求者的公网IP
                         */
                        val result = buffer.getString(0, buffer.length(), Charsets.UTF_8.name()) ?: "0.0.0.0|0.0.0.0"
                        val parts = result.split("|")
                        val publicNetworkAddress = parts[1]
                        future.complete(publicNetworkAddress)
                    }
                }
            }
        }

        override fun onAskDnsRecord(future: Future<String>) {
            val query = "/d?dn=$LOCAL_DO_NAME&clientip=1"
            httpClient.get(HOST_DNSPOD, query) { response: HttpClientResponse? ->
                response?.bodyHandler { buffer: Buffer? ->
                    if (buffer == null) {
                        future.fail(response.statusMessage())
                    } else {
                        /**
                         * 返回结果："0.0.0.0|0.0.0.0"
                         * 其中split("|")[0]为DNS的记录
                         *    split("|")[1]为请求者的公网IP
                         */
                        val result = buffer.getString(0, buffer.length(), Charsets.UTF_8.name()) ?: "0.0.0.0|0.0.0.0"
                        val parts = result.split("|")
                        val dnsRecords = parts[0]
                        future.complete(dnsRecords)
                    }
                }
            }
        }

        /**
         * https://cloud.tencent.com/document/product/302/8511
         */
        override fun onRequestUpdateDNS(future: Future<Boolean>) {
            TODO("unimplemented")

            val action = "RecordModify"
            val secretId = QCloud.secretId
            val region = "ap-guangzhou"
            val timestamp = System.currentTimeMillis()
            val nonce = Random(timestamp)
            val signature = ""
            val signatureMethod = "HmacSHA256"

            val operationDomain = "cocoonshu.com"
            val operationSubDomain = "*"
            val operationRecordType = ""
            val operationRecordLine = "默认"
            val operationRecordId = ""

            val query = """
                        ?Action=$action
                        &SecretId=$secretId
                        &Region=$region
                        &Timestamp=$timestamp
                        &Nonce=$nonce
                        &Signature=$signature
                        &SignatureMethod=$signatureMethod
                        
                        &
                        """.trim('\r', '\n', ' ')
            httpClient.get(HOST_QCLOUD, query) { response: HttpClientResponse? ->
                response?.bodyHandler { buffer: Buffer? ->

                }
            }
        }

    }

    override fun start() {
        super.start()
        println("[MAINTAIN] start Maintainer server")
        httpClient = vertx.createHttpClient()
        messager.start(vertx.eventBus())
    }

}