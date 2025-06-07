package db.inmemorystore.course

import db.BatchStatus
import db.ScheduleType
import db.Timeline
import java.util.Date

class BatchCourse(
    private val id: Int,
    private val parentCourseId: Int, // Foreign Key
    private val totalDays: Int,
    private val startDate: Date,
    private val endDate: Date,
    private val scheduleType: ScheduleType,
    private val status: BatchStatus,
    private val batchScheduleIds: List<Int>,
) : Timeline() {
    fun getId() = id

    fun getParentCourseId() = parentCourseId

    fun getTotalDays() = totalDays

    fun getStartDate() = startDate

    fun getEndDate() = endDate

    fun getScheduleType() = scheduleType

    fun getStatus() = status

    fun getBatchScheduleIds() = batchScheduleIds

    companion object {
        private val records = mutableMapOf<Int, BatchCourse>()
    }
}