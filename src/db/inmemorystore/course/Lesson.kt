package db.inmemorystore.course

import db.ResourceStatus
import db.Timeline
import db.inmemorystore.user.Student
import java.util.UUID

class Lesson (
    val id: Int,
    var sequenceNumber: Int,
    var title: String,
    var resource: String,
    var duration: Float,  // note: "duration in minutes"
    var status: ResourceStatus,
) : Timeline() {
    companion object {
        private val records = mutableMapOf<UUID, Student>()
    }
}