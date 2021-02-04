package com.cobox.iot.vulture.data

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

abstract class AbsDatabase : IDatabase {

    abstract val databasePath: String
    abstract val databaseVersion: Int
    lateinit var db: Connection

    override fun prepare() {
        Class.forName("org.sqlite.JDBC")
        ensureDatabaseFolder(databasePath)
        db = DriverManager.getConnection("jdbc:sqlite:$databasePath")
    }

    protected fun onPrepare() {}

    protected fun ensureDatabaseFolder(databasePath: String) {
        File(databasePath).parentFile.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs().also { isSuccess ->
                    println("[DATABASE] create database folder '${parent.absolutePath}' ${if (isSuccess) "success" else "failed"}")
                }
            } else if (!parent.isDirectory) {
                println("[DATABASE] database folder '${parent.absolutePath}' isn't a directory, " +
                        "please remove the file and try to launch Vulture again")
            } else if (!parent.canWrite()) {
                println("[DATABASE] database folder '${parent.absolutePath}' cannot be read, " +
                        "please override the reading access permission and try to launch Vulture again")
            } else { /* Folder is work normally, ignore */ }
        }
    }

    override fun hasCreatedDatabase(): Boolean =
        runCatching {
            db.createStatement().use { statement ->
                var hasTable = false
                var currentDbVersion = 0

                statement.executeQuery(
                    """
                        PRAGMA user_version
                    """.trimIndent()
                ).use { resultSet ->
                    if (!resultSet.isClosed) {
                        currentDbVersion = if (!resultSet.isClosed && resultSet.next()) {
                            resultSet.getInt("user_version")
                        } else {
                            currentDbVersion
                        }
                        println("[AUTH] database '$databasePath' version is $currentDbVersion" + if (currentDbVersion != databaseVersion) " not $currentDbVersion" else "")
                    }
                }

                statement.executeQuery(
                    """
                        SELECT name
                        FROM sqlite_master
                        WHERE type = 'table'
                        ORDER BY name
                    """.trimIndent()
                ).use { resultSet ->
                    hasTable = (!resultSet.isClosed and resultSet.next())
                }

                return hasTable and (currentDbVersion == databaseVersion)
            }
        }.getOrDefault(false)

    override fun close() {
        db.close()
    }

}