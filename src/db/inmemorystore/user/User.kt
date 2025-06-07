package db.inmemorystore.user

import core.user.schemas.NewUserData
import core.user.schemas.UserUpdateData
import db.Timeline
import db.UserRole
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

open class User (
    protected val id : UUID, // PK
    protected var firstName: String,
    protected var lastName: String,
    protected val email: String,
    protected var role: UserRole,
    protected var hashPassword: String,
    protected var status: UserStatus,
    protected var lastLoginAt: LocalDateTime,
): Timeline() {
    // Custom getters to avoid naming conflicts

    fun getUserId() = id

    fun getUserFirstName() = firstName

    fun getUserLastName() = lastName

    fun getUserEmail() = email

    fun getUserRole() = role

    fun getUserHashedPassword() = hashPassword

    fun getUserStatus() = status

    fun getUserLastLoginAt() = lastLoginAt

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
            val user = records.getValue(userId)

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