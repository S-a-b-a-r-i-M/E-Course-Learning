package core.user.repositories

import core.user.schemas.BaseUser
import core.user.schemas.NewUserData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

class UserRepo : AbstractUserRepo {
    companion object{
        private val userRecords = mutableMapOf<UUID, BaseUser>()
        private val emailToIdMap = mutableMapOf<String, UUID>()
    }

    // ******************* READ *********************
    override fun getUserByEmail(email: String): UserData? {
        val id = emailToIdMap[email] ?: return null
        return userRecords[id] as UserData
    }

    // ******************* CREATE *******************
    override fun createUser(newUserData: NewUserData): UserData {
        val user = UserData(
            id = UUID.randomUUID(),
            firstName = newUserData.firstName,
            lastName = newUserData.lastName,
            email = newUserData.email,
            role = newUserData.role,
            hashPassword = newUserData.hashedPassword,
            status = UserStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now()
        )
        userRecords[user.id] = user
        emailToIdMap[user.email] = user.id
        return user
    }

    // ******************* UPDATE *******************
    override fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean {
        val user = userRecords.getValue(userId)
        updateData.firstName?.let { user.firstName = it }
        updateData.lastName?.let { user.lastName = it }
        updateData.status?.let { user.status = it }
        return true
    }

    override fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean {
        userRecords.getValue(userId).lastLoginAt = lastLoginAt
        return true
    }

    // ******************* EXISTS *******************
    override fun isEmailExists(email: String): Boolean {
        return emailToIdMap[email] != null
    }
}
