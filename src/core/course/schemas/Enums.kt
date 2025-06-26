package core.course.schemas

import utils.capitalize
import kotlin.enums.EnumEntries

// Extension function on EnumEntries
fun <T: Enum<T>> EnumEntries<T>.fromString(inputValue: String, default: T): T {
    return find { it.name == inputValue.trim().uppercase() } ?: run {
        println("given value is not valid.Hence, assigning '${default.name.capitalize()}' by default")
        default
    }
}

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
}