package com.cobox.iot.vulture.iot

import com.cobox.iot.vulture.data.AbsDatabase

/**
 * TABLE [iot]:
 * =======================================================================
 * COLUMN    | TYPE | NOT NULL | PRIMARY KEY | FOREIGN KEY | AUTOINCREMENT
 * ----------|------|----------|-------------|-------------|--------------
 * -----------------------------------------------------------------------
 */
class IotDatabase(
    override val databasePath: String,
    override val databaseVersion: Int = VERSION
) : AbsDatabase() {

    companion object COLUMNS {
        const val VERSION    = 1

        const val TABLE      = "iot"
    }

    override fun onCreateTableDatabase() {
//        db.createStatement().use { statement ->
//            statement.execute(
//                """
//                    PRAGMA user_version = $VERSION;
//                """.trimIndent()
//            )
//            statement.executeUpdate(
//                """
//
//                """.trimIndent()
//            )
//            println("[AUTH] create table '$TABLE' in $databasePath with version $VERSION")
//        }
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