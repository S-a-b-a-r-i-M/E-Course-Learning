package db.inmemorystore.course

import core.course.schemas.NewCourseBasicData
import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import db.Timeline
import java.util.UUID
import kotlin.collections.mutableMapOf

val CURRENT_FILE_NAME = Throwable().stackTrace[0].fileName

open class Course (
    private val id : Int,
    private var title: String,
    private var description: String,
    private var duration : Float, //note: "duration in minutes"
    private val createdBy: UUID, // By which admin
    private var skills: List<String>,
    private var courseLevel: CourseLevel,
    private var courseType: CourseType,
    private var isFreeCourse: Boolean,
    private var status: ResourceStatus,
    private val categoryId: Int,
    private var prerequisites: List<String>? = null,
    private var priceDetailsId: Int? = null, // null - means no price details
    private val moduleIds: MutableList<Int> = mutableListOf(),
) : Timeline() {
    fun getId() = id

    fun getTitle() = title

    fun getDescription() = description

    fun getDuration() = duration

    fun getCreatedBy() = createdBy

    fun getSkills() = skills

    fun getCourseLevel() = courseLevel

    fun getCourseType() = courseType

    fun isFreeCourse() = isFreeCourse

    fun getStatus() = status

    fun getCategoryId() = categoryId

    fun getPrerequisites() = prerequisites?.toList() // returns a copy (immutable view)

    fun getPriceDetailsId() = priceDetailsId

    fun getModuleIds() = moduleIds.toList() // returns a copy (immutable view)

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
            println("$CURRENT_FILE_NAME: New course created")
            return course
        }

        fun updatePriceDetailsId(courseId: Int, priceDetailsId: Int) {
            records.getValue(courseId).priceDetailsId = priceDetailsId
            println("$CURRENT_FILE_NAME: Price details($priceDetailsId) updated into course($courseId)")
        }

        fun addModuleId(courseId: Int, moduleId: Int) {
            records.getValue(courseId).moduleIds.add(moduleId)
            println("$CURRENT_FILE_NAME: Module id($moduleId) added into Course($courseId)")
        }

        fun updateDuration(courseId: Int, duration: Float): Float {
            val course = records.getValue(courseId)
            course.duration += duration // Addition of duration
            return course.duration
        }

        fun getRecords(): Map<Int, Course> = records.toMap()
    }
}