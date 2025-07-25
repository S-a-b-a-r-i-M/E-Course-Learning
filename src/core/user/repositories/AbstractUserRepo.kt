package core.user.repositories

import core.user.schemas.BaseUser
import core.user.schemas.NewUserData
import core.user.schemas.StudentData
import core.user.schemas.UserData
import core.user.schemas.UserUpdateData
import java.time.LocalDateTime
import java.util.UUID

interface AbstractUserRepo {
    // ******************* CREATE *******************
    fun createStudentUser(newUserData: NewUserData): StudentData

    // ******************* READ *******************
    fun getUserByEmail(email: String): BaseUser?

    // ******************* UPDATE *******************
    fun updateUser(userId: UUID, updateData: UserUpdateData): Boolean
//    fun updateLastLogin(userId: UUID, lastLoginAt: LocalDateTime): Boolean

    // ******************* EXISTS *******************
    fun isEmailExists(email: String): Boolean
}