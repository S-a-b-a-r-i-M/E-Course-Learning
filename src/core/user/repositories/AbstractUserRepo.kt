package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import db.inmemorystore.user.User
import java.time.LocalDateTime
import java.util.UUID

interface AbstractUserRepo {
    fun isEmailExists(email: String): Boolean
    fun createUser(newUserData: NewUserData): UserData
//    fun getUserPasswordByEmail(email: String): String?
    fun getUserByEmail(email: String): UserData?
    fun updateUser(userId: UUID, updateDate: UserUpdateData): Boolean
    fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean
}