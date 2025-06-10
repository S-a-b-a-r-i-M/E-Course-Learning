package db

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