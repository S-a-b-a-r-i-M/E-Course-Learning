package db.inmemorystore.course

import db.CompletionStatus
import db.Timeline
import java.sql.Time
import java.util.Date
import java.util.UUID

class BatchSchedule (
    val id: Int,
    val moduleId: Int,
    val lessonId: Int,
    var tutorId: UUID,
    val scheduledDate: Date,
    val startTime: Time,
    val endTime: Time,
    val staus: CompletionStatus,
) : Timeline() {
    companion object {
        private val records = mutableMapOf<Int, Category>()
    }
}