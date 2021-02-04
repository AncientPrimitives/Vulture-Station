package com.cobox.iot.vulture.auth

import com.cobox.iot.vulture.data.AbsDatabase
import java.util.*

/**
 * TABLE [user_info]:
 * =====================================================================================
 * COLUMN              | TYPE     | NOT NULL | PRIMARY KEY | FOREIGN KEY | AUTOINCREMENT
 * --------------------|----------|----------|-------------|-------------|--------------
 * id                  | INT      |    √     |      √      |             |
 * username            | TEXT     |    √     |             |             |
 * secret              | TEXT     |    √     |             |             |
 * token               | TEXT     |    √     |             |             |
 * register_timestamp  | INT      |    √     |             |             |
 * last_auth_timestamp | INT      |          |             |             |
 * --------------------|----------|----------|-------------|-------------|--------------
 * nas_id              | INT      |          |             |      √      |
 * iot_id              | INT      |          |             |      √      |
 * -------------------------------------------------------------------------------------
 */
class AuthorityDatabase(
    override val databasePath: String,
    override val databaseVersion: Int = VERSION
) : AbsDatabase() {
    companion object COLUMNS {
        const val VERSION            = 1

        const val TABLE              = "user_info"
        const val ID                 = "id"
        const val USERNAME           = "username"
        const val SECRET             = "secret"
        const val TOKEN              = "auth_token"
        const val REGISTER_DATETIME  = "register_timestamp"
        const val LAST_AUTH_DATETIME = "last_auth_timestamp"
        const val NAS_ID             = "nas_id"
        const val IOT_ID             = "iot_id"

        const val NAS_TABLE          = "nas"
        const val NAS_RECORD_ID      = "record_id"

        const val IOT_TABLE          = "iot"
        const val IOT_RECORD_ID      = "id"
    }

    override fun onCreateTableDatabase() {
        db.createStatement().use { statement ->
            statement.execute(
                """
                    PRAGMA user_version = $VERSION;
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                    CREATE TABLE IF NOT EXISTS $TABLE (
                        $ID                 INTEGER  PRIMARY KEY AUTOINCREMENT,
                        $USERNAME           TEXT     NOT NULL,
                        $SECRET             TEXT     NOT NULL,
                        $TOKEN              TEXT     NOT NULL,
                        $REGISTER_DATETIME  INTEGER  NOT NULL,
                        $LAST_AUTH_DATETIME INTEGER,
                    );
                """.trimIndent()
            )
            println("[AUTH] create table '$TABLE' in $databasePath with version $VERSION")
        }
    }

    override fun onUpgradeDatabase(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun onDowngradeDatabase(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun onDropDatabase() {
        db.createStatement().use { statement ->
            statement.executeUpdate(
                """
                    DROP TABLE IF EXISTS $TABLE;
                """.trimIndent()
            )
        }
    }

    fun authority(key: String, secret: String): String = authorityImpl(key, secret, token = "")

    fun authority(token: String): String = authorityImpl("", "", token = token)

    private fun authorityImpl(key: String, secret: String, token: String = ""): String {
        val whereClause = if (token.isEmpty()) {
            """
                $USERNAME = '$key' 
                AND $SECRET = '$secret'
            """.trimIndent()
        } else {
            """
                $TOKEN = '$token'
            """.trimIndent()
        }

        db.createStatement().use { statement ->
            // 1. 查询是否存在此用户
            // 2. 查询此用户是否处于认证成功的状态
            // 3. 如果未处于认证成功，则更新认证时间为now

            var userToken: String = ""
            var lastAuthTimestamp: Long = 0
            statement.executeQuery(
                """
                    SELECT $TOKEN, $LAST_AUTH_DATETIME
                    FROM $TABLE
                    WHERE
                        $whereClause
                """.trimIndent()
            ).use { resultSet ->
                if (resultSet.isClosed) return ""

                if (resultSet.next()) {
                    val indexOfToken = resultSet.findColumn(TOKEN)
                    val indexOfLastAuthDate = resultSet.findColumn(LAST_AUTH_DATETIME)
                    userToken = resultSet.getString(indexOfToken)
                    lastAuthTimestamp = resultSet.getLong(indexOfLastAuthDate)
                }
            }

            return if (userToken.isEmpty()) { "" } else {
                val nowDateTime = Calendar.getInstance()
                val expireAuthDateTime = Calendar.getInstance().apply {
                    timeInMillis = lastAuthTimestamp
                    add(Calendar.MONTH, 1)
                }

                if (nowDateTime.after(expireAuthDateTime)) {
                    statement.executeUpdate(
                        """
                            UPDATE $TABLE
                            SET $LAST_AUTH_DATETIME = ${Calendar.getInstance().timeInMillis}
                            WHERE $TOKEN = '$userToken'
                        """.trimIndent()
                    )
                }

                userToken
            }
        }
    }

    fun unauthority(token: String): Boolean {
        db.createStatement().use { statement ->
            statement.executeUpdate(
                """
                    UPDATE $TABLE
                    SET $LAST_AUTH_DATETIME = null
                    WHERE $TOKEN = '$token'
                """.trimIndent()
            ).let { updatedRow ->
                return (updatedRow > 0)
            }
        }
    }

    fun register(key: String, secret: String, token: String): String {
        return db.createStatement().use { statement ->
            runCatching {
                val currentTime = Calendar.getInstance()
                statement.executeUpdate(
                    """
                    INSERT INTO $TABLE (
                        $USERNAME, $SECRET, $TOKEN,
                        $REGISTER_DATETIME, $LAST_AUTH_DATETIME
                    ) VALUES (
                        $key, $secret, $token,
                        ${currentTime.timeInMillis},
                        ${currentTime.timeInMillis}
                    )
                """.trimIndent()
                ).let { row ->
                    return if (row > 0) token else ""
                }
            }.getOrDefault("")
        }
    }

    fun hasUser(key: String): Boolean {
        db.createStatement().use { statement ->
            statement.executeQuery(
                """
                    SELECT $ID
                    FROM $TABLE
                    WHERE
                        $USERNAME = '$key'
                """.trimIndent()
            ).use { resultSet ->
                return resultSet.next()
            }
        }
    }

}