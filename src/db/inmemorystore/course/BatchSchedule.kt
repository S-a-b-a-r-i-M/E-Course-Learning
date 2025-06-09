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
    val scheduledDate: Date,
    val startTime: Time,
    val endTime: Time,
    private var tutorId: UUID,
    private var staus: CompletionStatus,
) : Timeline() {
    fun getTutorId() = tutorId

    fun getStatus() = staus

    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Category>()
    }
}