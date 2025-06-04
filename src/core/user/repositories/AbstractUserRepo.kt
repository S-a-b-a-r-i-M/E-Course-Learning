package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import db.inmemorystore.User
import java.util.UUID

interface AbstractUserRepo {
    fun isEmailExists(email: String): Boolean
    fun createUser(userData: NewUserData): User?
//    fun getUserPasswordByEmail(email: String): String?
    fun getUserByEmail(email: String): User?
    fun updateUser(userId: UUID, updateDate: UserUpdateData): Boolean
}