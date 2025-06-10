package core.user.repositories

import core.user.schemas.NewUserData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import java.time.LocalDateTime
import java.util.UUID

interface AbstractUserRepo {
    // ******************* READ *******************
    fun getUserByEmail(email: String): UserData?

    // ******************* CREATE *******************
    fun createUser(newUserData: NewUserData): UserData

    // ******************* UPDATE *******************
    fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean
    fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean

    // ******************* EXISTS *******************
    fun isEmailExists(email: String): Boolean
}