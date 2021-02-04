package com.cobox.iot.vulture.data

interface IDatabaseRepo {
    val databases: Map<String, IDatabase>
    fun createDatabaseIfNeed()
    fun upgradeDatabase()
    fun downgradeDatabase()
    fun deleteDatabase()
}