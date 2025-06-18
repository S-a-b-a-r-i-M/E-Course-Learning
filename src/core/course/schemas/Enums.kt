package core.course.schemas

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
    PAYMENT_FAILED
}