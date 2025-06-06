package db.inmemorystore.course

import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import db.ResourceStatus
import db.Timeline
import db.inmemorystore.user.Student
import java.util.UUID

class Lesson (
    val id: Int,
    var title: String,
    var resource: String,
    var duration: Float,  // note: "duration in minutes"
    var sequenceNumber: Int,
    var status: ResourceStatus,
) : Timeline() {
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
    }
}