package core.user.repositories

import core.user.schemas.BaseUser
import core.user.schemas.NewUserData
import core.user.schemas.StudentData
import core.user.schemas.UserData
import core.user.schemas.UserRole
import core.user.schemas.UserStatus
import core.user.schemas.UserUpdateData
import db.DatabaseManager
import java.time.LocalDateTime
import java.util.UUID
import kotlin.system.measureTimeMillis
import kotlin.use

import kotlinx.coroutines.*

class PersistableUserRepo : AbstractUserRepo {
    private val conn
        get() = DatabaseManager.getDBConnection()

    // ******************* CREATE *******************
    private fun createUser(newUserData: NewUserData): Pair<UUID, LocalDateTime> {
        // Prepare
        val sql = """
            INSERT INTO "User" (id, firstName, lastName, email, role, hashedPassword, lastLoginAt, status)
            VALUES (gen_random_uuid(), ?, ?, ?, ?::UserRole, ?, CURRENT_TIMESTAMP, ?::UserStatus)
            RETURNING id, lastLoginAt;
        """

        // Execute
        conn.prepareStatement(sql).use { pstmt ->
            // Add values
            pstmt.setString(1, newUserData.firstName)
            pstmt.setString(2, newUserData.lastName)
            pstmt.setString(3, newUserData.email)
            pstmt.setString(4, newUserData.role.name)
            pstmt.setString(5, newUserData.hashedPassword)
            pstmt.setString(6, UserStatus.ACTIVE.name)
            //Execute
            pstmt.executeQuery().use { rs ->
                rs.next()
                return Pair(
                    rs.getObject("id", UUID::class.java),
                    rs.getTimestamp("lastLoginAt").toLocalDateTime()
                )
            }
        }
    }

    override fun createStudentUser(newUserData: NewUserData): StudentData {
        // Prepare
        val (userId, lastLoginAt) = createUser(newUserData)
        val sql = """
            INSERT INTO Student (student_id, gitHubUrl, linkedInUrl)
            VALUES ('$userId', '', '')
        """

        // Execute
        conn.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }

        return StudentData(
            id = userId,
            firstName = newUserData.firstName,
            lastName = newUserData.lastName,
            email = newUserData.email,
            role = newUserData.role,
            status = UserStatus.ACTIVE,
            hashPassword = newUserData.hashedPassword,
            lastLoginAt = lastLoginAt,
        )
    }

    // ******************* READ *******************
    override fun getUserByEmail(email: String): BaseUser? {
        val sql = """SELECT * FROM "User" WHERE email=?"""

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, email)
            // Execute
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val userId = rs.getObject("id", UUID::class.java)
                    val firstName = rs.getString("firstName")
                    val lastName = rs.getString("lastName")
                    val email = rs.getString("email")
                    val hashPassword = rs.getString("hashedPassword")
                    val lastLoginAt = rs.getTimestamp("lastLoginAt").toLocalDateTime()
                    val role = UserRole.getFromString(rs.getString("role"))
                    val status = UserStatus.getFromString(rs.getString("status"))

                    return when (role) {
                        UserRole.ADMIN -> UserData(
                            userId, firstName, lastName, email, role, status, hashPassword, lastLoginAt
                        )

                        UserRole.STUDENT -> StudentData(
                            userId, firstName, lastName, email, role, status, hashPassword, lastLoginAt
                        )

                        UserRole.TRAINER -> TODO()
                    }
                }
                else
                    return null
            }
        }
    }

    // ******************* UPDATE *******************
    override fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean {
        TODO("Not yet implemented")
    }

    // ******************* EXISTS *******************
    override fun isEmailExists(email: String): Boolean {
        val sql = """SELECT EXISTS (
            SELECT 1 FROM "User" WHERE email=?
        )"""

        // Execute
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, email)
            return pstmt.executeQuery().use { rs ->
                rs.next()
                rs.getBoolean(1)
            }
        }
    }
}

class AsyncUserRepo {
    private val conn
        get() = DatabaseManager.getDBConnection()

    private suspend fun createUser(newUserData: NewUserData): Pair<UUID, LocalDateTime> {
        // Prepare
        val sql = """
            INSERT INTO "User" (id, firstName, lastName, email, role, hashedPassword, lastLoginAt, status)
            VALUES (gen_random_uuid(), ?, ?, ?, ?::UserRole, ?, CURRENT_TIMESTAMP, ?::UserStatus)
            RETURNING id, lastLoginAt;
        """

        // Execute
        return withContext(Dispatchers.IO) {
            conn.prepareStatement(sql).use { pstmt ->
                // Add values
                pstmt.setString(1, newUserData.firstName)
                pstmt.setString(2, newUserData.lastName)
                pstmt.setString(3, newUserData.email)
                pstmt.setString(4, newUserData.role.name)
                pstmt.setString(5, newUserData.hashedPassword)
                pstmt.setString(6, UserStatus.ACTIVE.name)
                //Execute
                pstmt.executeQuery().use { rs ->
                    rs.next()
                    Pair(
                        rs.getObject("id", UUID::class.java),
                        rs.getTimestamp("lastLoginAt").toLocalDateTime()
                    )
                }
            }
        }
    }

    suspend fun createStudentUser(newUserData: NewUserData): StudentData {
        // Prepare
        return withContext(Dispatchers.IO) {
            val (userId, lastLoginAt) = createUser(newUserData)
            val sql = """
                INSERT INTO Student (student_id, gitHubUrl, linkedInUrl)
                VALUES ('$userId', '', '')
            """

            // Execute
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(sql)
            }

            StudentData(
                id = userId,
                firstName = newUserData.firstName,
                lastName = newUserData.lastName,
                email = newUserData.email,
                role = newUserData.role,
                status = UserStatus.ACTIVE,
                hashPassword = newUserData.hashedPassword,
                lastLoginAt = lastLoginAt,
            )
        }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val sql = """SELECT EXISTS (
                SELECT 1 FROM "User" WHERE email=?
            )"""

            // Execute
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, email)
                pstmt.executeQuery().use { rs ->
                    rs.next()
                    rs.getBoolean(1)
                }
            }
        }
    }
}

fun main() {
    val repo = AsyncUserRepo()
    val numberOfRecords = 10000
    val timeTaken = measureTimeMillis {
        runBlocking(Dispatchers.IO) {
            for (i in 0 until numberOfRecords) {
                launch {
                    val newUser = NewUserData(
                        firstName = "firstName$i",
                        lastName = "lastName$i",
                        email = "email$i",
                        hashedPassword = "hashedPassword$i",
                        role = UserRole.STUDENT,
                    )
                    repo.createStudentUser(newUser)
                }
            }
        }
    }
    println("$numberOfRecords records inserted in ${timeTaken / 1000.0} sec")

    /*
    val repo = PersistableUserRepo()
    val numberOfRecords = 10000
    val timeTaken = measureTimeMillis {
        for (i in 1 until numberOfRecords) {
            val newUser = NewUserData (
                firstName="firstName$i",
                lastName="lastName$i",
                email="email$i",
                hashedPassword="hashedPassword$i",
                role=UserRole.STUDENT,
            )
            repo.createStudentUser(newUser)
        }
    }
    // 10000 records inserted in 3.358 sec
    println("$numberOfRecords records inserted in ${timeTaken / 1000.0} sec")
    */

    DatabaseManager.closeConnection()
}