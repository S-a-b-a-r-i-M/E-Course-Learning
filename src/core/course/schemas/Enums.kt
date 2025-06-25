package core.course.schemas

import config.LogLevel
import config.logInfo
import core.user.schemas.CURRENT_FILE_NAME

enum class ResourceStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVE;

    companion object {
        fun getFromStrValue(value: String): ResourceStatus = when {
            value.equals("DRAFT", true) -> DRAFT
            value.equals("PUBLISHED", true) -> PUBLISHED
            value.equals("ARCHIVE", true) -> ARCHIVE
            else -> {
                println("Invalid level, defaulting to Published")
                PUBLISHED
            }
        }
    }
}

enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    companion object {
        fun getFromStrValue(value: String): CourseLevel = when {
            value.equals("BEGINNER", true) -> INTERMEDIATE
            value.equals("INTERMEDIATE", true) -> INTERMEDIATE
            value.equals("ADVANCED", true) -> ADVANCED
            else -> {
                println("Invalid level, defaulting to Beginner")
                BEGINNER
            }
        }
    }
}

enum class CourseType {
    LIVE,
    SELF_PACED;

    companion object {
        fun getFromStrValue(value: String): CourseType = when {
            value.equals("LIVE", true) -> LIVE
            value.contains("SELF", true) -> SELF_PACED
            else -> {
                println("Invalid level, defaulting to self paced")
                SELF_PACED
            }
        }
    }
}


enum class EnrollmentStatus {
    ASSIGNED,
    NOT_ASSIGNED,
    PAYMENT_FAILED;

    companion object {
        fun getFromString(str: String): EnrollmentStatus = when (str.trim().uppercase()) {
            "ASSIGNED" -> ASSIGNED
            "NOT_ASSIGNED" -> NOT_ASSIGNED
            "PAYMENT_FAILED" -> PAYMENT_FAILED
            else -> {
                logInfo(
                    "$CURRENT_FILE_NAME: given user status is not found.So assigning 'Student' by default",
                    LogLevel.WARNING
                )
                ASSIGNED
            }
        }
    }
}