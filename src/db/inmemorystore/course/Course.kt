package db.inmemorystore.course

import core.course.schemas.NewCourseBasicData
import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import db.Timeline
import java.util.UUID
import kotlin.collections.mutableMapOf

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
    val categoryId: Int,
    var prerequisites: List<String>? = null,
    var priceDetailsId: Int? = null, // null - means no price details
    val moduleIds: MutableList<Int> = mutableListOf(),
) : Timeline() {
    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Course>()

        fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course {
            val course = Course(
                id = serialId++,
                title = newCourseData.title,
                description = newCourseData.description,
                duration = 0.0f,// Duration will be calculated based on module duration
                createdBy = currentUserId,
                skills = newCourseData.skills,
                courseLevel = newCourseData.courseLevel,
                courseType = newCourseData.courseType,
                isFreeCourse = newCourseData.isFreeCourse,
                status = ResourceStatus.DRAFT,
                prerequisites = newCourseData.prerequisites,
                categoryId = newCourseData.categoryId
            )

            records[course.id] = course
            println("New course created")
            return course
        }

        fun updatePriceDetailsId(courseId: Int, priceDetailsId: Int) {
            records.getValue(courseId).priceDetailsId = priceDetailsId
            println("Course.kt: Price details($priceDetailsId) updated into course($courseId)")
        }

        fun addModuleId(courseId: Int, moduleId: Int) {
            records.getValue(courseId).moduleIds.add(moduleId)
            println("Course.kt: Module id($moduleId) added into Course($courseId)")
        }

        fun getRecords(): Map<Int, Course> = records.toMap()
    }
}