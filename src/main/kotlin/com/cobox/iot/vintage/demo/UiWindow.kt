package com.cobox.iot.vintage.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.DriverManager

class UiWindow {

    private val title = ""

    init {
        GlobalScope.launch {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:test.db").use { connection ->
                connection.createStatement().use { statement ->
                    statement.apply {
                        executeUpdate(
                            """
                                DROP TABLE IF EXISTS USER_INFO
                            """.trimIndent()
                        )
                        executeUpdate(
                            """
                                CREATE TABLE USER_INFO (
                                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                    NAME TEXT NOT NULL,
                                    SECRET TEXT NOT NULL
                                )
                            """.trimIndent()
                        )
                        executeUpdate(
                            """
                                INSERT INTO USER_INFO (
                                    NAME, SECRET
                                ) VALUES (
                                    'Cocoonshu', 'ODltaWs3'
                                )
                            """.trimIndent()
                        )
                        executeQuery(
                            """
                                SELECT *
                                FROM USER_INFO
                            """.trimIndent()
                        ).use { resultSet ->
                            while (resultSet.next()) {
                                println("ID: ${resultSet.getInt(1)}, NAME: ${resultSet.getString(2)}, SECERT: ${resultSet.getString(3)}")
                            }
                        }

                    }
                }
            }
        }
    }
}