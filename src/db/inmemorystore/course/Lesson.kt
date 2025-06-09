package db.inmemorystore.course

import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import db.ResourceStatus
import db.Timeline
import db.inmemorystore.user.Student
import java.util.UUID

class Lesson (
    val id: Int,
    private var title: String,
    private var resource: String,
    private var duration: Float,  // note: "duration in minutes"
    private var sequenceNumber: Int,
    private var status: ResourceStatus,
) : Timeline() {
    fun getTittle() = title

    fun getResource() = resource

    fun getDuration() = duration

    fun getSequenceNumber() = sequenceNumber

    fun getStatus() = status

    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Lesson>()

        fun createLesson(newLessonData: NewLessonData): Lesson {
            val lesson = Lesson(
                id = serialId++,
                title = newLessonData.title,
                resource = newLessonData.resource,
                duration = newLessonData.duration,
                sequenceNumber = newLessonData.sequenceNumber,
                status = newLessonData.status,
            )

            records[lesson.id] = lesson
            println("Module.kt: New module added(id-${lesson.id})")
            return lesson
        }

        fun getRecords() = records.toMap()
    }
}