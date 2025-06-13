package core.user.repositories

import core.user.schemas.BaseUser
import core.user.schemas.NewUserData
import core.user.schemas.StudentData
import core.user.schemas.TrainerData
import core.user.schemas.UserData
import core.user.schemas.UserRole
import core.user.schemas.UserUpdateData
import core.user.schemas.UserStatus
import java.time.LocalDateTime
import java.util.UUID

class UserRepo : AbstractUserRepo {
    companion object{
        // Main Store
        private val userRecords = mutableMapOf<UUID, BaseUser>()
        private val adminRecords = mutableMapOf<UUID, UserData>()
        private val studentRecords = mutableMapOf<UUID, StudentData>()
        private val trainerRecords = mutableMapOf<UUID, TrainerData>()
        // Mapping Store
        private val userIdToRole = mutableMapOf<UUID, UserRole>()
        private val emailToIdMap = mutableMapOf<String, UUID>()
    }

    // ******************* READ *********************
    fun getUser(userId: UUID): BaseUser {
        val role = userIdToRole.getValue(userId)
        return when (role) {
            UserRole.ADMIN -> adminRecords.getValue(userId)
            UserRole.STUDENT -> studentRecords.getValue(userId)
            UserRole.TRAINER -> trainerRecords.getValue(userId)
        }
    }

    override fun getUserByEmail(email: String): BaseUser? {
        val userId = emailToIdMap[email] ?: return null
        return getUser(userId)
    }

    // ******************* CREATE *******************
    override fun createStudentUser(newUserData: NewUserData): StudentData {
        val student = StudentData(
            id = UUID.randomUUID(),
            firstName = newUserData.firstName,
            lastName = newUserData.lastName,
            email = newUserData.email,
            role = newUserData.role,
            hashPassword = newUserData.hashedPassword,
            status = UserStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now(),
            gitHubUrl = null,
            linkedInUrl = null
        )
        // Add into store
        studentRecords[student.id] = student
        userIdToRole[student.id] = student.role
        emailToIdMap[student.email] = student.id
        return student
    }

    // ******************* UPDATE *******************
    override fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean {
        val user = userRecords.getValue(userId)
        val firstName = updateData.firstName ?: user.firstName
        val lastName = updateData.lastName ?: user.lastName
        val status = updateData.status ?: user.status

        // TODO: If needs, split update functions specific to user roles
        when (user) {
            is UserData -> {
                adminRecords[userId] = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    status = status
                )
            }
            is StudentData -> {
                studentRecords[userId] = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    status = status
                )
            }
            is TrainerData -> {
                trainerRecords[userId] = user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    status = status
                )
            }
        }

        return true
    }

    override fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean {
        TODO("Not yet implemented")
    }

    // ******************* EXISTS *******************
    override fun isEmailExists(email: String): Boolean {
        return emailToIdMap[email] != null
    }
}
