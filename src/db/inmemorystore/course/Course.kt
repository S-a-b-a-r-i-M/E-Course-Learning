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
    var prerequisites: List<String>? = null,
    var priceDetailsId: Int? = null, // null - means no price details
    var categoryIds: MutableList<Int> = mutableListOf<Int>(),
    var moduleIds: MutableList<Int> = mutableListOf<Int>(),
) : Timeline() {
    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Course>()

        fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course {
            val course = Course (
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
                categoryIds = newCourseData.categoryIds.toMutableList()
            )

            records[course.id] = course
            println("New course created")
            return course
        }

        fun addPriceDetailsIds(courseId: Int, priceDetailsId: Int): Boolean {
            val course = records[courseId]
            if (course == null) return false

            course.priceDetailsId = priceDetailsId
            return true
        }

        fun getRecords(): Map<Int, Course> = records.toMap()
    }
}

class Category (
    val id: Int,
    val name: String
) : Timeline() {
    companion object {
        private var serial = 1
        private val records = mutableMapOf<Int, Category>()

        init {
            records[serial] = Category(serial++, "Software Development")
            records[serial] = Category(serial++, "Testing")
            records[serial] = Category(serial++, "English Communication")
            records[serial] = Category(serial++, "Web Development")
            records[serial] = Category(serial++, "Mobile App Development")
            records[serial] = Category(serial++, "Drawing")
            records[serial] = Category(serial++, "Others")
        }

        fun createCategory(name: String): Category {
            val category = Category(serial++, name)
            records[category.id] = category
            return category
        }

        fun getRecords(): Map<Int, Category> = records.toMap()
    }
}