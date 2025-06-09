package db.inmemorystore.course

import db.BatchStatus
import db.ScheduleType
import db.Timeline
import java.util.Date

class BatchCourse(
    val id: Int,
    val parentCourseId: Int, // Foreign Key
    val totalDays: Int,
    val startDate: Date,
    val endDate: Date,
    val scheduleType: ScheduleType,
    private var status: BatchStatus,
    val batchScheduleIds: List<Int>,
) : Timeline() {
    fun getStatus() = status

    companion object {
        private val records = mutableMapOf<Int, BatchCourse>()
    }
}