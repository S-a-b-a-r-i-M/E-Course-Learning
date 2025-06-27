package core.course.schemas

import utils.fromString

enum class ResourceStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVE;

    companion object {
        fun getFromString(value: String): ResourceStatus = entries.fromString(value, PUBLISHED)
    }
}

enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    companion object {
        fun getFromString(value: String): CourseLevel = entries.fromString(value, BEGINNER)
    }
}

enum class CourseType {
    LIVE,
    SELF_PACED;

    companion object {
        fun getFromString(value: String): CourseType = entries.fromString(value, SELF_PACED)
    }
}


enum class EnrollmentStatus {
    ASSIGNED,
    NOT_ASSIGNED,
    PAYMENT_FAILED;

    companion object {
        fun getFromString(str: String): EnrollmentStatus = entries.fromString(str, ASSIGNED)
    }
}

enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun getFromString(str: String): CompletionStatus = entries.fromString(str, NOT_STARTED)
    }
}