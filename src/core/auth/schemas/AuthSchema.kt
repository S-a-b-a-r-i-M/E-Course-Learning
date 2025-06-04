package core.auth.schemas

import java.util.UUID

data class SignUpData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
)

data class SignInData(
    val email: String,
    val password: String,
)

data class LogOutModel(val userId: UUID)