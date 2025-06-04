package db.inmemorystore.course

import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import db.Timeline
import java.util.UUID

open class Course (
    val id : Int,
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
    val moduleIds: List<Int> = mutableListOf<Int>(),
    val priceDetailsIds: List<Int>? = null, // null - means no price details(free course)
) : Timeline() {
    companion object {
        private val records = mutableMapOf<Int, Course>()
    }
}

class Category (
    id: Int,
    name: String
) : Timeline() {
    companion object {
        private val records = mutableMapOf<Int, Category>()
    }
}