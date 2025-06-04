package core.user.schemas

import db.UserRole
import db.UserStatus
import java.time.LocalDateTime

data class NewUserData (
    val firstName: String,
    val lastName: String,
    val email: String,
    val hashedPassword: String,
    val role: UserRole,
)

data class UserUpdateData (
    var firstName: String? = null,
    var lastName: String? = null,
    var status: UserStatus? = null,
    var lastLoginAt: LocalDateTime? = null,
)