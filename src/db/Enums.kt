package db

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
    ADVANCED,
}

enum class CourseType {
    LIVE,
    SELF_PACED
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