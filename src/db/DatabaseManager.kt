package db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseManager {
    // DATABASE CONFIGS
    private const val DB_NAME = "e_learning_db"
    private const val DB_PASSWORD = "root"
    private const val DB_USER_NAME = "postgres"
    private const val DB_URL = "jdbc:postgresql://localhost:5432/$DB_NAME"
    // CONNECTION
    private var connection: Connection? = null

    /** Gets a database connection, creating one if needed or if current is invalid */
    fun getDBConnection(): Connection {
//        connection.isValid(5)
        if (connection == null) {
            connection = createConnection()
        }
        return connection!!
    }

    /** Creates a new database connection */
    private fun createConnection(): Connection {
        return try {
            DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD).also {
                connection = it
            }
        } catch (e: SQLException) {
            throw RuntimeException("Failed to connect to database", e)
        }
    }

    /** Closes existing database connection */
    fun closeConnection() {
        connection?.close()
        connection = null
    }
}

//fun main() {
//    val conn = DatabaseManager.getDBConnection()
//    val readSql = "SELECT * FROM t_user"
//    try { // use { ... } in Kotlin is equivalent to Java’s try-with-resources — it auto-closes the resource.
//        conn.createStatement().use { stmt ->
//            stmt.executeQuery(readSql).use { rs ->
//                while (rs.next()) {
//                    println("id: " + rs.getInt("id"));
//                    println("name: " + rs.getString("userName"));
//                    println("email: " + rs.getString("email") + "\n");
//                }
//            }
//        }
//    } catch (e: SQLException) {
//        println("Err in read: ${e.message}")
//    }
//}


