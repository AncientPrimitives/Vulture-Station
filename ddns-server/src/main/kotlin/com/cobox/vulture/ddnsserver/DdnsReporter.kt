package com.cobox.vulture.ddnsserver

import com.cobox.utilites.log.Log
import com.cobox.vulture.standard.dns.dnspod.DnsPod
import com.cobox.vulture.standard.dns.dnspod.PublicAddress.queryPublicIP
import com.cobox.vulture.standard.xutil.NetAddress.EMPTY_ADDRESS
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.future.SucceededFuture
import io.vertx.core.json.JsonObject
import java.net.InetAddress

class DdnsReporter(
    private val vertx: Vertx,
    private val eventBus: EventBus,
    private val config: JsonObject = JsonObject()
) {

    companion object {
        const val TAG = "DdnsReporter"

        private const val MSG_QUERY_PUBLIC_IP = "/local/dns_reporter/query_public_ip"
        private const val MSG_REPORT_IP_TO_DNS = "/local/dns_reporter/report_ip_to_dns"
        private const val MSG_UPDATE_DNS = "/local/dns_reporter/update_dns"

        private const val NO_JOB = 0L
        private const val VAL_SHOULD_REPORT_TO_DNS = true
        private const val VAL_JUST_UPDATE_PUBLIC_UP = false
    }

    private var dnsReportInterval: Long = VultureConfig.Default.REPORT_INTERVAL_SEC * 1000
    private lateinit var adapter: DdnsAdapter
    private var dnsDaemonJob: Long = NO_JOB
    private var lastDnsAddress: InetAddress = EMPTY_ADDRESS

    fun prepare() {
        val vendor = config.getJsonObject(VultureConfig.Key.VENDOR)
        val vendorName = vendor.getString(VultureConfig.Key.VENDOR_NAME)
        dnsReportInterval = config.getLong(VultureConfig.Key.REPORT_INTERVAL_SEC, VultureConfig.Default.REPORT_INTERVAL_SEC) * 1000
        adapter = when (vendorName.toLowerCase()) {
            "dnspod" -> {
                Log.info(TAG, "[prepare] DDNS vendor '$vendorName' adapter created")
                DnsPodAdapter(vertx, vendor)
            }
            else -> {
                Log.error(TAG, "[prepare] DDNS vendor '$vendorName' isn't adapted yet")
                DummyAdapter()
            }
        }
        adapter.initVendor()
    }

    fun launch() {
        eventBus.let { bus ->
            bus.localConsumer(MSG_QUERY_PUBLIC_IP) { msg: Message<Boolean> ->
                val shouldReportToDnsAfterQuery = msg.body()
                vertx.executeBlocking<Void> { task ->
                    detectPublicAddressChange { isChanged, newestAddress ->
                        if (isChanged && shouldReportToDnsAfterQuery) {
                            Log.info(TAG, "[MSG_QUERY_PUBLIC_IP] public address change has been detected, report to DNS")
                            bus.publish(MSG_REPORT_IP_TO_DNS, newestAddress.hostAddress)
                        } else {
                            Log.info(TAG, "[MSG_QUERY_PUBLIC_IP] public address checked, no change, wait for ${dnsReportInterval / 1000L} seconds")
                        }

                        task.complete()
                    }
                }
            }

            bus.localConsumer(MSG_REPORT_IP_TO_DNS) { msg: Message<String> ->
                val hostAddress = InetAddress.getByName(msg.body())
                vertx.executeBlocking<Void> { task ->
                    reportAddressToDns(hostAddress) { isSuccess ->
                        if (isSuccess) lastDnsAddress = hostAddress
                        task.complete()
                    }
                }
            }

            bus.localConsumer(MSG_UPDATE_DNS) { msg: Message<String> ->
                bus.publish(MSG_QUERY_PUBLIC_IP, VAL_SHOULD_REPORT_TO_DNS)
            }
        }

        // 初始化任务序列
        vertx.executeBlocking<Boolean> { promise ->
            vertx.eventBus().publish(MSG_UPDATE_DNS, null)
            dnsDaemonJob = vertx.setPeriodic(dnsReportInterval) { jobId ->
                vertx.eventBus().publish(MSG_UPDATE_DNS, null)
            }

            promise.complete()
        }
    }

    fun shutdown() {
        vertx.cancelTimer(dnsDaemonJob)
        dnsDaemonJob = NO_JOB
    }

    fun destroy() {
        adapter.releaseVendor()
    }

    fun queryPublicAddress(handler: Handler<AsyncResult<InetAddress>>) {
        if (lastDnsAddress == EMPTY_ADDRESS) {
            vertx.executeBlocking<InetAddress> { task ->
                detectPublicAddressChange { isChanged, newestAddress ->
                    if (isChanged) {
                        eventBus.publish(MSG_UPDATE_DNS, null)
                    }

                    if (newestAddress.hostAddress == EMPTY_ADDRESS.hostAddress) {
                        handler.handle(Future.failedFuture(""))
                    } else {
                        handler.handle(SucceededFuture(newestAddress))
                    }
                    task.complete()
                }
            }
        } else {
            handler.handle(SucceededFuture(lastDnsAddress))
        }
    }

    fun refresh(handler: Handler<AsyncResult<Void>>) {
        Log.debug(TAG,"[refresh] detect and report public address to DDNS immediately")
        eventBus.publish(MSG_UPDATE_DNS, null)
        handler.handle(Future.succeededFuture())
    }

    private fun detectPublicAddressChange(
        onChanged: ((isChanged: Boolean, publicAddress: InetAddress) -> Unit)
    ) {
        vertx.queryPublicIP { address ->
            address?.let {
                onChanged(address.hostAddress != lastDnsAddress.hostAddress, address)
            } ?: let {
                onChanged(false, lastDnsAddress)
                return@queryPublicIP
            }
        }
    }

    private fun reportAddressToDns(address: InetAddress, result: ((isSuccess: Boolean) -> Unit)) {
        Log.debug(TAG,"[reportAddressToDns] Report public ip: ${address.hostAddress} to DNS")
        val reportDomain = config.getString(VultureConfig.Key.REPORT_DOMAIN, VultureConfig.Default.REPORT_DOMAIN)
        adapter.updateARecord(reportDomain, address) { isSuccess ->
            Log.info(TAG, "[reportAddressToDns] update Dns record with ${address.hostAddress} for '$reportDomain', ${if (isSuccess) "success" else "failed"}")
            result(isSuccess)
        }
    }

    /**
     * DDNSAdapter的DnsPod实现
     */
    private class DnsPodAdapter(
        vertx: Vertx,
        config: JsonObject
    ) : DdnsAdapter {

        private val dnsPod = DnsPod(
            vertx,
            id = config.getString("api_key", ""),
            token = config.getString("api_token", "")
        )

        override fun initVendor() { }

        override fun updateARecord(
            domain: String,
            address: InetAddress,
            result: (isSuccess: Boolean) -> Unit
        ) = dnsPod.updateARecord(domain, address, isDDns = true, result)

        override fun queryARecord(
            domain: String,
            result: (ip: InetAddress) -> Unit
        ) = dnsPod.queryARecord(domain, result)

        override fun releaseVendor() = dnsPod.terminate()
    }

    /**
     * DDNSAdapter空实现
     */
    private class DummyAdapter: DdnsAdapter {
        override fun initVendor() {}

        override fun updateARecord(
            domain: String,
            address: InetAddress,
            result: (isSuccess: Boolean) -> Unit
        ) {
            result(false)
        }

        override fun queryARecord(domain: String, result: (ip: InetAddress) -> Unit) {
            result(EMPTY_ADDRESS)
        }

        override fun releaseVendor() {}
    }

}