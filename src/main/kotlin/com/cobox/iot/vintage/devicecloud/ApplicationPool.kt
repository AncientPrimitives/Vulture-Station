package com.cobox.iot.vintage.devicecloud

import java.nio.charset.Charset
import java.util.concurrent.locks.ReentrantLock


/**
 * Pool structure:
 *    root
 *      └─ app
 *          ├─ Group ┬ user
 *          |    │   └ user
 *          |    └──── messagePool
 *          |
 *          └─ Group ┬ user
 *               │   └ user
 *               └──── messagePool
 */
class ApplicationPool private constructor() {

    private val DataLock = ReentrantLock()

    private val authPool: HashMap<String, ByteArray> = HashMap()
    private val appPool: HashMap<String, Group> = HashMap()
    private val userLink: HashMap<String, MqttSession> = HashMap()
    private val deactiveLink: MutableSet<MqttSession> = HashSet()
    public  val sessionCount: Int = calcSessionCount()

    companion object {
        public fun createFrom(dbFile: String): ApplicationPool {
            // TODO
            val pool = ApplicationPool()
            run { // Dummy data
                pool.authPool["Cocoonshu"] = "89mik7".toByteArray(Charset.forName("utf8"))
            }
            return pool
        }
    }

    fun auth(username: String, password: ByteArray): Boolean
            = authPool[username]?.contentEquals(password)?: false

    fun lookupSession(clientId: String): MqttSession? = runWithDataLock {
        userLink[clientId]
    }

    fun lookupSession(session: MqttSession): MqttSession? = runWithDataLock {
        val applicationKey = session.endpoint.auth().userName() ?: null
        appPool[applicationKey]?.get(session.clientId)
    }

    fun registerSession(session: MqttSession): Boolean = runWithDataLock {
        val applicationKey = session.endpoint.auth().userName() ?: return@runWithDataLock false

        val group: Group = if (appPool[applicationKey] == null) {
            val newbee = Group(applicationKey)
            appPool[applicationKey] = newbee
            newbee
        } else {
            appPool[applicationKey]!!
        }
        group[session.clientId] = session
        userLink[session.clientId] = session
        true
    }

    fun unregisterSession(session: MqttSession): Boolean = runWithDataLock {
        if (userLink[session.clientId] == null) {
            return@runWithDataLock false
        }

        val applicationKey = session.endpoint.auth().userName() ?: return@runWithDataLock false
        appPool[applicationKey]?.remove(session.clientId)
        userLink.remove(session.clientId)
        deactiveLink.remove(session)
        true
    }

    fun replaceSession(session: MqttSession): Boolean = registerSession(session)

    fun markAsDeactived(session: MqttSession) = runWithDataLock {
        session.disposeEndpoint()
        val existedSession = lookupSession(session)
        if (existedSession != null) {
            deactiveLink.add(existedSession)
        }
    }

    private fun calcSessionCount(): Int {
        var totalUserCount = 0
        appPool.entries.forEach {
            totalUserCount += it.value.userCount
        }
        return totalUserCount
    }

    private fun <R> runWithDataLock(block: () -> R): R {
        DataLock.tryLock()
        try {
            return block.invoke()
        } finally {
            DataLock.unlock()
        }
    }

    private class Group(val applicationName: String) {
        private val userSet: MutableMap<String, MqttSession> = HashMap()

        val userCount: Int = userSet.size

        operator fun get(clientId: String): MqttSession? = userSet[clientId]

        operator fun set(clientId: String, value: MqttSession) {
            userSet[clientId] = value
        }

        fun remove(clientId: String) = userSet.remove(clientId)
    }
}