package core.user.schemas

import utils.fromString

enum class UserRole() {
    ADMIN,
    TRAINER,
    STUDENT;

    companion object {
        fun getFromString(str: String): UserRole = entries.fromString(str, STUDENT)
    }
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    companion object {
        fun getFromString(str: String): UserStatus = entries.fromString(str, ACTIVE)
    }
}

