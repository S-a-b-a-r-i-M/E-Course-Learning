package db

import db.CourseLevel.ADVANCED
import db.CourseLevel.BEGINNER
import db.CourseLevel.INTERMEDIATE

enum class UserRole {
    ADMIN,
    TRAINER,
    STUDENT,
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

enum class ResourceStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVE,
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
                println("Invalid level, defaulting to BEGINNER")
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
            value.equals("SELF_PACED", true) -> SELF_PACED
            value.equals("SELF-PACED", true) -> SELF_PACED
            else -> {
                println("Invalid level, defaulting to SELF_PACED")
                SELF_PACED
            }
        }
    }
}

enum class ScheduleType {
    WEEKDAYS_ONLY,
    WEEKENDS_ONLY,
    DAILY,
}

enum class BatchStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}