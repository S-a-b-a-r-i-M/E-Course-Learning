package core.auth.models

import java.util.UUID

data class SignUpModel(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
)

data class SignInModel(
    val email: String,
    val password: String,
)

data class LogOutModel(val userId: UUID)