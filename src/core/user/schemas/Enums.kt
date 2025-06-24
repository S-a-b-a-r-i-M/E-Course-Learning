package core.user.schemas

import config.LogLevel
import config.logInfo

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName
enum class UserRole() {
    ADMIN,
    TRAINER,
    STUDENT;

    companion object {
        fun getFromString(str: String): UserRole = when (str.trim().uppercase()) {
            "ADMIN" -> ADMIN
            "TRAINER" -> TRAINER
            "STUDENT" -> STUDENT
            else -> {
                logInfo(
                    "$CURRENT_FILE_NAME: given user role is not found.So assigning 'Student' by default",
                    LogLevel.WARNING
                )
                STUDENT
            }
        }
    }
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    companion object {
        fun getFromString(str: String): UserStatus = when (str.trim().uppercase()) {
            "ACTIVE" -> ACTIVE
            "INACTIVE" -> INACTIVE
            "SUSPENDED" -> SUSPENDED
            else -> {
                logInfo(
                    "$CURRENT_FILE_NAME: given user status is not found.So assigning 'Student' by default",
                    LogLevel.WARNING
                )
                ACTIVE
            }
        }
    }
}

