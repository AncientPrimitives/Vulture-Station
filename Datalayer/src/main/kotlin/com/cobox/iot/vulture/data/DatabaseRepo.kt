package com.cobox.iot.vulture.data

import com.cobox.iot.vulture.application.Application
import com.cobox.iot.vulture.auth.AuthorityDatabase
import com.cobox.iot.vulture.iot.IotDatabase
import com.cobox.iot.vulture.nas.NasDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DatabaseRepo(
    private val app: Application
) : IDatabaseRepo {

    companion object Databases {
        const val NAS_DB = "nas.db"
        const val AUTH_DB = "authority.db"
        const val IOT_DB = "iot.db"
    }

    override val databases: Map<String, IDatabase> = mapOf(
        "nas" to NasDatabase(File("${app.configuration.database.root}/$NAS_DB").canonicalPath),
        "auth" to AuthorityDatabase(File("${app.configuration.database.root}/$AUTH_DB").canonicalPath),
        "iot" to IotDatabase(File("${app.configuration.database.root}/$IOT_DB").canonicalPath)
    )

    private val databasesCreateOrdering = arrayOf(
        "nas", "iot", "auth"
    )

    init {
        GlobalScope.launch(Dispatchers.IO) {
            createDatabaseIfNeed()
        }
    }

    /**
     * 内部数据库的创建是顺序相关的，需依照databasesCreateOrdering指定的顺序执行
     */
    override fun createDatabaseIfNeed() {
        databasesCreateOrdering.forEach { key ->
            databases[key]?.also { database ->
                runCatching {
                    database.prepare()
                    if (!database.hasCreatedDatabase()) {
                        database.onCreateTableDatabase()
                    }
                }
            }
        }
    }

    override fun upgradeDatabase() {
        TODO()
    }

    override fun downgradeDatabase() {
        TODO()
    }

    override fun deleteDatabase() {
        databasesCreateOrdering.forEach { key ->
            databases[key]?.also { database ->
                runCatching {
                    database.onDropDatabase()
                }
            }
        }
    }

}