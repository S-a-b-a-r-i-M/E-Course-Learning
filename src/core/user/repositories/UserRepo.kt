package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import db.inmemorystore.user.User
import java.time.LocalDateTime
import java.util.UUID

class UserRepo : AbstractUserRepo {
    // ******************* READ *********************
    override fun getUserByEmail(email: String): UserData? {
        val id = User.getEmailToIdMap()[email] ?: return null
        val user = User.getRecords()[id] ?: return null
        return convertUserToUserData(user)
    }

    // ******************* CREATE *******************
    override fun createUser(newUserData: NewUserData): UserData {
        val user: User = User.create(newUserData)
        return convertUserToUserData(user)
    }

    // ******************* UPDATE *******************
    override fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean {
        return User.update(userId, updateData)
    }

    override fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean {
        return User.updateLastLogin(userId, lastLoginAt)
    }

    // ******************* EXISTS *******************
    override fun isEmailExists(email: String): Boolean {
        return User.getEmailToIdMap()[email] != null
    }

    // Helper Functions
    private fun convertUserToUserData(user: User) : UserData = UserData(
        user.id,
        user.getUserFirstName(),
        user.getUserLastName(),
        user.email,
        user.role,
        user.getUserStatus(),
        user.hashPassword
    )
}
