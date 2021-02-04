package com.cobox.iot.vulture.maintain

import com.cobox.iot.vintage.communicate.Event
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus

open class MaintianerMessager {

    companion object {
        const val IP_CONFIG = "/system/maintainer/ipconfig"
        const val ASK_IP_CONFIG = 0x0001
        const val ASK_DNS_RECORD = 0x0002
        const val ASK_PUBLIC_NETWORK_ADDRESS = 0x0003
        const val CMD_UPDATE_DNS = 0x0004
    }

    private lateinit var eventBus: EventBus

    fun start(bus: EventBus) {
        eventBus = bus
        Event.registerToEventBus(eventBus)
        setupMessageMap()
    }

    private fun setupMessageMap() {
        eventBus.consumer<Event>(IP_CONFIG) { event ->
            when (event.body().what) {
                ASK_IP_CONFIG -> {
                    val future: Future<String> = Future.future()
                    future.setHandler { result: AsyncResult<String>? ->
                        if (result?.succeeded() == true) {
                            event.reply(Event(ASK_IP_CONFIG, result.result()))
                        } else {
                            event.fail(ASK_IP_CONFIG, result?.cause()?.message)
                        }
                    }
                    onAskIpConfig(future)
                }

                ASK_DNS_RECORD -> {
                    val future: Future<String> = Future.future()
                    future.setHandler { result: AsyncResult<String>? ->
                        if (result?.succeeded() == true) {
                            event.reply(Event(ASK_DNS_RECORD, result.result()))
                        } else {
                            event.fail(ASK_DNS_RECORD, result?.cause()?.message)
                        }
                    }
                    onAskDnsRecord(future)
                }

                ASK_PUBLIC_NETWORK_ADDRESS -> {
                    val future: Future<String> = Future.future()
                    future.setHandler { result: AsyncResult<String>? ->
                        if (result?.succeeded() == true) {
                            event.reply(Event(ASK_PUBLIC_NETWORK_ADDRESS, result.result()))
                        } else {
                            event.fail(ASK_PUBLIC_NETWORK_ADDRESS, result?.cause()?.message)
                        }
                    }
                    onAskPublicNetworkAddress(future)
                }

                CMD_UPDATE_DNS -> {
                    val future: Future<Boolean> = Future.future()
                    future.setHandler { result: AsyncResult<Boolean>? ->
                        if (result?.succeeded() == true) {
                            event.reply(Event(CMD_UPDATE_DNS, result.result()))
                        } else {
                            event.fail(CMD_UPDATE_DNS, result?.cause()?.message)
                        }
                    }
                    onRequestUpdateDNS(future)
                }

                else -> event.reply(Event(-1))
            }
        }

    }

    open fun onAskIpConfig(future: Future<String>) { future.failed() }
    open fun onAskPublicNetworkAddress(future: Future<String>) { future.failed() }
    open fun onAskDnsRecord(future: Future<String>) { future.failed() }
    open fun onRequestUpdateDNS(future: Future<Boolean>) { future.failed() }

}