package com.cobox.iot.vulture.nas

import com.cobox.iot.vulture.data.AbsDatabase

/**
 * TABLE [nas]:
 * =========================================================================
 * COLUMN      | TYPE | NOT NULL | PRIMARY KEY | FOREIGN KEY | AUTOINCREMENT
 * ------------|------|----------|-------------|-------------|--------------
 * record_id   | INT  |    √     |      √      |             |      √
 * owner_id    | INT  |    √     |             |             |               // 指向user_info.id
 * url         | TEXT |    √     |             |             |
 * url_hash    | INT  |    √     |             |             |
 * mime_type   | TEXT |    √     |             |             |
 * media_type  | INT  |          |             |             |
 * bucket_hash | INT  |          |             |             |               // 指向nas.record_id
 * -------------------------------------------------------------------------
 */
class NasDatabase(
    override val databasePath: String,
    override val databaseVersion: Int = VERSION
) : AbsDatabase() {

    companion object COLUMNS {
        const val VERSION     = 1

        const val TABLE       = "nas"
        const val RECORD_ID   = "record_id"
        const val OWNER_ID    = "owner_id"
        const val URL         = "url"
        const val URL_HASH    = "hash"
        const val MIME_TYPE   = "mime_type"
        const val MEDIA_TYPE  = "media_type"
        const val BUCKET_HASH = "bucket_hash"

        const val INVALID_ID  = -1
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
                        $RECORD_ID   INTEGER PRIMARY KEY AUTOINCREMENT,
                        $OWNER_ID    INTEGER NOT NULL,
                        $URL         TEXT    NOT NULL,
                        $MIME_TYPE   TEXT    NOT NULL,
                        $MEDIA_TYPE  INTEGER,
                        $URL_HASH    INTEGER NOT NULL,
                        $BUCKET_HASH INTEGER
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

}