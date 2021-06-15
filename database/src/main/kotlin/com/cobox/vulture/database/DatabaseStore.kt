package com.cobox.vulture.database

import com.cobox.utilites.annotation.BlockThread
import com.cobox.utilites.log.Log
import com.cobox.vulture.standard.script.SqlScriptReader
import io.vertx.core.*
import io.vertx.core.impl.future.FailedFuture
import io.vertx.core.impl.future.SucceededFuture
import io.vertx.core.json.JsonObject
import io.vertx.jdbcclient.JDBCPool
import java.io.File
import java.lang.StringBuilder

class DatabaseStore(
    private val vertx: Vertx,
    private val config: JsonObject
) {

    companion object {
        const val TAG = "DatabaseStore"
    }

    private val pool by lazy {
        val databaseRepo = config.getString(
            VultureConfig.Key.DATABASE_REPO,
            VultureConfig.Default.DATABASE_REPO
        )
        val databaseUser = config.getString(
            VultureConfig.Key.DATABASE_USER,
            VultureConfig.Default.DATABASE_USER
        )
        val databasePassword = config.getString(
            VultureConfig.Key.DATABASE_PASSWORD,
            VultureConfig.Default.DATABASE_PASSWORD
        )
        val databaseUrl = "jdbc:sqlite:$databaseRepo"

        ensureDatabaseDirectory()
        Log.info(TAG, "[init] JDBC connect to '$databaseUrl'")
        kotlin.runCatching {
            JDBCPool.pool(
                vertx,
                JsonObject().apply {
                    put("url", databaseUrl)
                    put("driver_class", "org.sqlite.SQLiteJDBCLoader")
                    put("max_pool_size", 16)
                    put("user", databaseUser)
                    put("password", databasePassword)
                }
            )
        }.onFailure {
            Log.error(TAG, "[init] JDBC pool create failed, caused by ${it.cause}", it.cause)
        }.getOrNull()
    }

    fun start(handler: Handler<AsyncResult<Void>>?) {
        createDatabaseIfNecessary {
            insertDefaultValuesIfNecessary {
                dumpTablesInDatabase {
                    handler?.handle(SucceededFuture(null))
                }
            }
        }
    }

    fun stop(handler: Handler<AsyncResult<Void>>?) {
        pool?.close {
            handler?.handle(it)
        }
    }

    @BlockThread
    private fun createDatabaseIfNecessary(handler: Handler<AsyncResult<Boolean>>) {
        val createScriptPath = config.getString(VultureConfig.Key.DATABASE_CREATE_SCRIPT, VultureConfig.Default.DATABASE_CREATE_SCRIPT)
        ensureDatabaseDirectory()
        executeSqlScript(createScriptPath)
        handler.handle(SucceededFuture(true))
    }

    @BlockThread
    private fun insertDefaultValuesIfNecessary(handler: Handler<AsyncResult<Boolean>>) {
        val initializeScriptPath = config.getString(VultureConfig.Key.DATABASE_INITIALIZE_SCRIPT, VultureConfig.Default.DATABASE_INITIALIZE_SCRIPT)
        executeSqlScript(initializeScriptPath)
        handler.handle(SucceededFuture(true))
    }

    @BlockThread
    private fun dumpTablesInDatabase(handler: Handler<AsyncResult<Boolean>>) {
        Log.info(TAG, "[start] Query table summary...")
        pool?.query("SELECT name FROM sqlite_master where type='table' order by name")
            ?.execute {
                if (it.failed()) {
                    Log.error(
                        TAG,
                        "[start] connect to Sqlite Database failed, caused by ${it.cause()}",
                        it.cause()
                    )
                    handler.handle(FailedFuture(it.cause()))
                    return@execute
                }

                val builder = StringBuilder()
                val dataset = it.result()
                dataset.forEach { row ->
                    builder.append(row.getString(0)).append("|")
                }

                if (builder.isNotEmpty()) {
                    builder.deleteCharAt(builder.lastIndex)
                }

                Log.info(TAG, "[start] found ${dataset.rowCount()} tables: $builder")
                handler.handle(SucceededFuture(true))
            }
    }

    private fun ensureDatabaseDirectory() {
        val databaseRepo = config.getString(
            VultureConfig.Key.DATABASE_REPO,
            VultureConfig.Default.DATABASE_REPO
        )

        File(databaseRepo).parentFile.let { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    private fun executeSqlScript(scriptPath: String) {
        vertx.fileSystem().let { fs ->
            if (!fs.existsBlocking(scriptPath)) return@let

            fs.readFileBlocking(scriptPath).let { buffer ->
                SqlScriptReader(buffer).let { reader ->
                    Log.info(TAG, "[executeSqlScript] exec '$scriptPath'")
                    pool?.getConnection { it ->
                        it.result().let { connection ->
                            kotlin.runCatching {
                                while (!reader.isEOF) {
                                    val sql = reader.readLine()
                                    connection.query(sql).execute { queryResult ->
                                        if (queryResult.failed()) {
                                            Log.error(TAG, "[executeSqlScript] exec: '$sql'...failed, caused by ${queryResult.cause()}")
                                        } else {
                                            Log.info(TAG, "[executeSqlScript] exec: '$sql'...success")
                                        }
                                    }
                                }
                            }.onFailure {
                                Log.error(TAG, "[executeSqlScript] ${it.cause}", it)
                            }
                            connection.close()
                        }
                    }
                    Log.info(TAG, "[executeSqlScript] exec '$scriptPath', finished")
                }
            }
        }
    }

}
