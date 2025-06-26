package db

import config.LogLevel
import config.logInfo

enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun getFromString(str: String): CompletionStatus = when (str.trim().uppercase()) {
            "NOT_STARTED" -> NOT_STARTED
            "IN_PROGRESS" -> IN_PROGRESS
            "COMPLETED" -> COMPLETED
            else -> {
                logInfo(
                     "given user status is not found.So assigning 'Student' by default",
                    LogLevel.WARNING
                )
                NOT_STARTED
            }
        }
    }
}