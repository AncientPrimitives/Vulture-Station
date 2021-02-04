package com.cobox.iot.vulture.data

public interface IDatabase {

    fun prepare()

    fun hasCreatedDatabase(): Boolean

    fun onCreateTableDatabase()

    fun onUpgradeDatabase(from: Int, to: Int)

    fun onDowngradeDatabase(from: Int, to: Int)

    fun onDropDatabase()

    fun close()

}