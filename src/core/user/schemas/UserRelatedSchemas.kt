package core.user.schemas

data class NewUserData (
    val firstName: String,
    val lastName: String,
    val email: String,
    val hashedPassword: String,
    val role: UserRole,
)

data class UserUpdateData (
    val firstName: String? = null,
    val lastName: String? = null,
    val status: UserStatus? = null,
)