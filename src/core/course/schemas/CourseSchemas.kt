package core.course.schemas

import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import java.util.UUID


data class NewCourseData (
    var title: String,
    var description: String,
    var duration : Float, //note: "duration in minutes"
    val createdBy: UUID, // By which admin
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var isFreeCourse: Boolean,
    var status: ResourceStatus,
    var categoryIds: List<Int>,
    var prerequisites: List<String>? = null,
    val priceDetailsIds: List<Int>? = null
)