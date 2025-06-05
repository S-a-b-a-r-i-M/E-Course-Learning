package db.inmemorystore.user

import core.user.schemas.NewUserData
import core.user.schemas.UserUpdateData
import db.Timeline
import db.UserRole
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

open class User (
    val id : UUID, // PK
    var firstName: String,
    var lastName: String,
    val email: String,
    var role: UserRole,
    var hashPassword: String,
    var status: UserStatus,
    var lastLoginAt: LocalDateTime,
): Timeline() {
    companion object {
        private val records = mutableMapOf<UUID, User>()
        private val emailToIdMap = mutableMapOf<String, UUID>()

        fun create(newUserData: NewUserData): User {
            val user = User(
                id = UUID.randomUUID(),
                firstName = newUserData.firstName,
                lastName = newUserData.lastName,
                email = newUserData.email,
                role = newUserData.role,
                hashPassword = newUserData.hashedPassword,
                status = UserStatus.ACTIVE,
                lastLoginAt = LocalDateTime.now()
            )

            records[user.id] = user
            emailToIdMap[user.email] = user.id
            return user
        }

        fun update(userId: UUID, updateData: UserUpdateData): Boolean {
            val user = records[userId]
            if (user == null) return false

            updateData.firstName?.let { user.firstName = it }
            updateData.lastName?.let { user.lastName = it }
            updateData.status?.let { user.status = it }
            updateData.lastLoginAt?.let { user.lastLoginAt = it }
            return true
        }

        fun delete(){

        }

        fun getRecords(): Map<UUID, User> = records.toMap()

        fun getEmailToIdMap(): Map<String, UUID> = emailToIdMap.toMap()
    }
}