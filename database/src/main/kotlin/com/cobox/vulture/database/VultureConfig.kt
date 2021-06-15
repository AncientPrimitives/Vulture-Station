package com.cobox.vulture.database

/**
 * /config/database.config = {
 *   "database_repo": "/storage/vulture/database/vulture.db",
 *   "database_user": "vulture",
 *   "database_password": "dnVsdHVyZQ",
 *   "database_create_script": "./config/create_database_script.sql",
 *   "database_initialize_script": "./config/default_records_script.sql"
 * }
 */
object VultureConfig {

    object Key {
        const val DATABASE_REPO = "database_repo"
        const val DATABASE_USER: String = "database_user"
        const val DATABASE_PASSWORD: String = "database_password"
        const val DATABASE_CREATE_SCRIPT: String = "database_create_script"
        const val DATABASE_INITIALIZE_SCRIPT: String = "database_initialize_script"
    }

    object Default {
        const val DATABASE_REPO = "./data/database/vulture.db" // "/storage/vulture/database/vulture.db"
        const val DATABASE_USER: String = "vulture"
        const val DATABASE_PASSWORD: String = "dnVsdHVyZQ"
        const val DATABASE_CREATE_SCRIPT: String = "./config/create_database_script.sql"
        const val DATABASE_INITIALIZE_SCRIPT: String = "./config/default_records_script.sql"
    }

}

