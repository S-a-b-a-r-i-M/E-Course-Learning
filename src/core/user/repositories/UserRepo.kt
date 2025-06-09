package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserUpdateData
import db.inmemorystore.user.User
import java.time.LocalDateTime
import java.util.UUID

class UserRepo : AbstractUserRepo {

    override fun isEmailExists(email: String): Boolean {
        return User.getEmailToIdMap()[email] != null
    }

    override fun createUser(newUserData: NewUserData): User? {
        return User.create(newUserData)
    }

    override fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean {
        return User.update(userId, updateData)
    }

    override fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean {
        return User.updateLastLogin(userId, lastLoginAt)
    }

    override fun getUserByEmail(email: String): User? {
        val id = User.getEmailToIdMap()[email] ?: return null
        return User.getRecords()[id]
    }
}
