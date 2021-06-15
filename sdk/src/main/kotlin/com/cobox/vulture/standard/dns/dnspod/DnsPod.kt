package com.cobox.vulture.standard.dns.dnspod

import com.cobox.utilites.log.Log
import com.cobox.vulture.standard.xutil.XUtil
import com.cobox.vulture.standard.xutil.vertx.AsyncResultUtil.onFailure
import com.cobox.vulture.standard.xutil.vertx.AsyncResultUtil.onSuccess
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import java.net.InetAddress

enum class DnsPodResultCode(val code: Int) {
    LOGIN_FAILED(-1),
    USAGE_EXCEEDED(-2),
    ILLEGAL_AGENT(-3),
    ILLEGAL_DELEGATE(-4),
    PERMISSION_DENIED(-7),
    TOO_MANY_BAD_LOGIN(-8),
    KEEP_CLOSE(-99),
    SUCCESS(1),
    ONLY_POST_ALLOW(2),
    UNKNOWN(3),
    ILLEGAL_USER_ID(6),
    ILLEGAL_USER(7),
    REJECT_FOR_ACCOUNT_LOCKING(83),
    REJECT_FOR_MULTI_LOCATION_LOGIN(85)
}

class DnsPod(
    private val vertx: Vertx,
    private val id: String,
    private val token: String,
    httpClient: HttpClient? = null
) {

    companion object {
        const val TAG = "DnsPod"
    }

    private val webClientOptions = WebClientOptions().apply {
        this.userAgent = "Vulture/1.0.0" // "curl/7.64.0"
        this.isUserAgentEnabled = true
    }

    private val webClient = httpClient?.let {
        WebClient.wrap(httpClient, webClientOptions)
    } ?: let {
        WebClient.create(vertx, webClientOptions)
    }

    fun queryVersion(result: ((version: String) -> Unit)) {
        val url = "https://dnsapi.cn/Info.Version"
        val param = "login_token=$id,$token&format=json"
        Log.debug(TAG, "[queryVersion] curl -X POST $url -d '$param'")

        webClient
            .postAbs(url)
            .putHeader(HttpHeaders.CONTENT_LENGTH.toString(), param.length.toString())
            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded")
            .putHeader(HttpHeaders.ACCEPT.toString(), "*/*")
            .sendBuffer(
                Buffer.buffer(param)
            ) {
                it.onSuccess { response ->
                    Log.debug(TAG, "[queryVersion] ${response.bodyAsString()}")
                    result(
                        kotlin.runCatching {
                            var version = "unknown"
                            response.bodyAsJsonObject().let { json ->
                                json.getJsonObject("status").let { status ->
                                    version = status.getString("message", "unknown")
                                }
                            }
                            version
                        }.getOrDefault("unknown")
                    )
                }.onFailure { throwable ->
                    Log.error(TAG, "query version failed, caused by ${throwable.message}", throwable)
                    result("unknown")
                }
            }
    }

    fun queryDnsInfo(domain: String, result: ((result: String) -> Unit)) {
        val url = "https://dnsapi.cn/Domain.Info"
        val param = "login_token=$id,$token&format=json&domain=$domain"
        Log.debug(TAG, "[queryVersion] curl -X POST $url -d '$param'")

        webClient
            .postAbs(url)
            .putHeader(HttpHeaders.CONTENT_LENGTH.toString(), param.length.toString())
            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded")
            .putHeader(HttpHeaders.ACCEPT.toString(), "*/*")
            .sendBuffer(
                Buffer.buffer(param)
            ) {
                it.onSuccess { response ->
                    Log.debug(TAG, "[queryVersion] ${response.bodyAsString()}")
                    result(
                        kotlin.runCatching {
                            response.bodyAsString()
                        }.getOrDefault("xxxx")
                    )
                }.onFailure { throwable ->
                    Log.error(TAG, "query DNS info failed, caused by ${throwable.message}", throwable)
                    result("xxxx")
                }
            }
    }

    private data class Record(
        val address: InetAddress = XUtil.NetAddress.EMPTY_ADDRESS,
        val recordId: String = XUtil.String.EMPTY_TEXT
    )

    private fun queryDnspodARecord(domain: String, result: ((record: Record) -> Unit)) {
        val url = "https://dnsapi.cn/Record.List"
        val param = "login_token=$id,$token&format=json&domain=$domain&record_type=A"
        Log.debug(TAG, "[queryDnspodARecord] curl -X POST $url -d '$param'")

        webClient
            .postAbs(url)
            .putHeader(HttpHeaders.CONTENT_LENGTH.toString(), param.length.toString())
            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded")
            .putHeader(HttpHeaders.ACCEPT.toString(), "*/*")
            .sendBuffer(
                Buffer.buffer(param)
            ) {
                it.onSuccess { response ->
                    Log.debug(TAG, "[queryDnspodARecord] ${response.bodyAsString()}")
                    result(
                        kotlin.runCatching {
                            var recordAddress = "0.0.0.0"
                            var recordId = ""
                            response.bodyAsJsonObject().let { json ->
                                val isDomainMatched = json.getJsonObject("domain").let { status ->
                                    (status.getString("name", "") == domain)
                                }
                                if (!isDomainMatched) return@onSuccess
                                json.getJsonArray("records").let { records ->
                                    val recordsCount = records.count()
                                    kotlin.run loop@{
                                        for (i in 0 until recordsCount) {
                                            records.getJsonObject(i).let { record ->
                                                if (record.getString("type", "") == "A") {
                                                    recordAddress = record.getString("value", "0.0.0.0")
                                                    recordId = record.getString("id", "")
                                                    return@loop
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Record(InetAddress.getByName(recordAddress), recordId)
                        }.getOrDefault(Record())
                    )
                }.onFailure { throwable ->
                    Log.error(TAG, "[queryDnspodARecord] query dns records failed, caused by ${throwable.message}", throwable)
                    result(Record())
                }
            }
    }

    private fun updateDnspodARecord(domain: String, record: Record, isDDns: Boolean = false, result: ((isOkay: Boolean) -> Unit)) {
        val url = if (isDDns) "https://dnsapi.cn/Record.Ddns" else "https://dnsapi.cn/Record.Modify"
        val param = "login_token=$id,$token&format=json&domain=$domain&record_id=${record.recordId}&sub_domain=*&value=${record.address.hostAddress}&record_type=A&record_line_id=0"
        Log.debug(TAG, "[updateDnspodARecord] curl -X POST $url -d '$param'")

        webClient
            .postAbs(url)
            .putHeader(HttpHeaders.CONTENT_LENGTH.toString(), param.length.toString())
            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded")
            .putHeader(HttpHeaders.ACCEPT.toString(), "*/*")
            .sendBuffer(
                Buffer.buffer(param, Charsets.UTF_8.name())
            ) {
                it.onSuccess { response ->
                    Log.debug(TAG, "[updateDnspodARecord] ${response.bodyAsString()}")
                    result(
                        kotlin.runCatching {
                            response.bodyAsJsonObject().let { json ->
                                json.getJsonObject("status").let { status ->
                                    status.getString("code", "${DnsPodResultCode.UNKNOWN}").toInt().let { code ->
                                        (code == DnsPodResultCode.SUCCESS.code)
                                    }
                                }
                            }
                        }.getOrDefault(false)
                    )
                }.onFailure { throwable ->
                    Log.error(TAG, "[updateDnspodARecord] update A record failed, caused by ${throwable.message}", throwable)
                    result(false)
                }
            }
    }

    /**
     * 查询A记录
     * @param domain 要查询的域名
     * @return 域名对应的A记录的IP地址
     */
    fun queryARecord(domain: String, result: ((ip: InetAddress) -> Unit)) {
        queryDnspodARecord(domain) { record ->
            result(record.address)
        }
    }

    /**
     * 更新A记录
     * @param domain 要更新的域名
     * @param address 要更新的IP地址
     * @param isDDns 是否为DDNS，如果是，则会为A记录更新在一个最短的TTL
     * @return DNS服务器返回的更新是否成功的回执
     */
    fun updateARecord(domain: String, address: InetAddress, isDDns: Boolean = false, result: ((isSuccess: Boolean) -> Unit)) {
        queryDnspodARecord(domain) { record ->
            if (record.recordId.isBlank()) {
                Log.error(TAG, "[updateARecord] cannot query dns record for $domain, should add new record instead of updating it")
                result(false)
                return@queryDnspodARecord
            }
            if (address.hostAddress == record.address.hostAddress) {
                Log.info(TAG, "[updateARecord] ignore updating request because the address of record isn't change")
                result(true)
                return@queryDnspodARecord
            }

            updateDnspodARecord(
                domain,
                Record(address = address, recordId = record.recordId),
                isDDns
            ) { isSuccess ->
                result(isSuccess)
            }
        }
    }

    fun terminate() {
        webClient.close()
    }

}