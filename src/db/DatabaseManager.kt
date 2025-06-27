package db

import config.LogLevel
import config.logInfo
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseManager {
    // DATABASE CONFIGS
    private const val DB_NAME = "e_learnin_db"
    private const val DB_PASSWORD = "root"
    private const val DB_USER_NAME = "postgres"
    private const val DB_URL = "jdbc:postgresql://localhost:5432/$DB_NAME"
    // CONNECTION
    @Volatile
    private var connection: Connection? = null

    /** Gets a database connection, creating one if needed or if current is invalid */
    fun getDBConnection(): Connection {
        return connection ?: synchronized(this) {
            connection ?: createConnection().also { connection = it }
        }
    }

    /** Creates a new database connection */
    private fun createConnection(): Connection {
        return try {
            DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD).also {
                connection = it
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to connect to database: ${e.message}", e)
        }
    }

    /** Closes existing database connection */
    fun closeConnection() {
        connection?.close()
        logInfo("Connection Closed... ${connection?.isClosed}", LogLevel.INFO)
        connection = null
    }
}



