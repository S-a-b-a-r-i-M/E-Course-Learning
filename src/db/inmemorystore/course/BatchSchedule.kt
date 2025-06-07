package db.inmemorystore.course

import db.CompletionStatus
import db.Timeline
import java.sql.Time
import java.util.Date
import java.util.UUID

class BatchSchedule (
    private val id: Int,
    private val moduleId: Int,
    private val lessonId: Int,
    private var tutorId: UUID,
    private val scheduledDate: Date,
    private val startTime: Time,
    private val endTime: Time,
    private val staus: CompletionStatus,
) : Timeline() {
    fun getId() = id

    fun getModuleId() = moduleId

    fun getLessonId() = lessonId

    fun getTutorId() = tutorId

    fun getScheduledDate() = scheduledDate

    fun getStartTime() = startTime

    fun getEndTime() = endTime

    fun getStatus() = staus

    companion object {
        private val records = mutableMapOf<Int, Category>()
    }
}