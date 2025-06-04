package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserUpdateData
import db.UserStatus
import db.inmemorystore.user.User
import java.time.LocalDateTime
import java.util.UUID

class UserRepo : AbstractUserRepo {

    override fun isEmailExists(email: String): Boolean {
        return User.getEmailToIdMap()[email] != null
    }

    override fun createUser(userData: NewUserData): User? {
        val user = User(
            id = UUID.randomUUID(),
            firstName = userData.firstName,
            lastName = userData.lastName,
            email = userData.email,
            role = userData.role,
            hashPassword = userData.hashedPassword,
            status = UserStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now()
        )

        return if (User.create(user)) user else null
    }

    override fun updateUser(userId: UUID, updateModel: UserUpdateData): Boolean {
        val updateDateMap = mapOf<String, Any>()
        return User.update(userId, updateDateMap)
    }

    override fun getUserByEmail(email: String): User? {
        val id = User.getEmailToIdMap()[email] ?: return null
        return User.getRecords()[id]
    }
}



//    override fun getUserPasswordByEmail(email: String): String? {
//        val userId: UUID? = UserTable.getEmailToIdMap()[email]
//        return if(userId != null) UserTable.getRecords()[userId]?.hashPassword else null
//    }
