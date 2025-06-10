package core.course.repositories

import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.schemas.PriceDetailsData
import db.ResourceStatus
import java.util.UUID

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

class CourseRepo : AbstractCourseRepo {
    companion object {
        // Serial Ids
        private var categorySerialId = 1
        private var priceDetailsSerialId = 1
        private var courseSerialId = 1
        private var moduleSerialId = 1
        private var lessonSerialId = 1

        private fun getNextCategoryId() = categorySerialId++
        private fun getNextPriceDetailsId() = priceDetailsSerialId++
        private fun getNextCourseId() = courseSerialId++
        private fun getNextModuleId() = moduleSerialId++
        private fun getNextLessonId() = lessonSerialId++

        // Map Storage
        private val categoryRecords = mutableMapOf<Int, CategoryData>()
        private val courseRecords = mutableMapOf<Int, DetailedCourseData>()
        private val priceDetailsIdToCourseId = mutableMapOf<Int, Int>()
        private val moduleIdToCourseId = mutableMapOf<Int, Int>()
        private val lessonIdToModuleId = mutableMapOf<Int, Int>()
    }

    // ******************* READ *********************
    override fun getCategory(id: Int): CategoryData = categoryRecords.getValue(id)

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData> {
        val endIndex = (offset + 1) * limit
        val categories = categoryRecords.values.toList()

        // Apply Search
        val result = categories.filter { it.name.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourses(searchQuery: String, offset: Int, limit: Int): List<DetailedCourseData> {
        val endIndex = (offset + 1) * limit
        val courses = courseRecords.values.toList()

        // Apply Search
        val result = courses.filter { it.title.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourse(courseId: Int): DetailedCourseData? {
        val course = courseRecords[courseId]
        if (course == null)
            println("$CURRENT_FILE_NAME: Course not found: courseId=$courseId")
        return course
    }

    fun getModule(moduleId: Int): ModuleData? {
        val courseId = moduleIdToCourseId[moduleId]
        if (courseId == null) {
            println("$CURRENT_FILE_NAME[getModule]: No course found for module id($moduleId)")
            return null
        }
        return courseRecords.getValue(courseId).modules.find { it.id == moduleId }
    }

    // ******************* CREATE *******************
    override fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData {
        val course = DetailedCourseData(
            id = getNextCourseId(),
            title = newCourseData.title,
            description = newCourseData.description,
            duration = 0,// Duration will be calculated based on module duration
            createdBy = currentUserId,
            skills = newCourseData.skills,
            courseLevel = newCourseData.courseLevel,
            courseType = newCourseData.courseType,
            isFreeCourse = newCourseData.isFreeCourse,
            status = ResourceStatus.DRAFT,
            prerequisites = newCourseData.prerequisites,
            category = newCourseData.category
        )

        courseRecords[course.id] = course
        println("$CURRENT_FILE_NAME: New course created")
        return course
    }

    override fun createCategory(name: String): CategoryData {
        val category = CategoryData(getNextCategoryId(), name)
        categoryRecords[category.id] = category
        return category
    }

    override fun createPriceDetails(newPriceData: NewPriceData, courseId: Int): PriceDetailsData? {
        val course = getCourse(courseId)
        if (course == null) return null
        // Add price-details id into course
        course.priceDetails = PriceDetailsData(
            id = getNextPriceDetailsId(),
            currencyCode = newPriceData.currencyCode,
            currencySymbol = newPriceData.currencySymbol,
            amount = newPriceData.amount
        )
        return course.priceDetails
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData {
        val course = courseRecords.getValue(courseId)

        val module = ModuleData(
            id = getNextModuleId(),
            title = newModuleData.title,
            description = newModuleData.description,
            sequenceNumber = newModuleData.sequenceNumber,
            status = newModuleData.status,
        )
        println("$CURRENT_FILE_NAME: New module created(id-${module.id})")
        // Add module id into course
        course.modules.add(module)
        println("$CURRENT_FILE_NAME: New module attached to course(${courseId})")
        return module
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData? {
        val module = getModule(moduleId)
        if (module == null) {
            println("$CURRENT_FILE_NAME: Lesson creation failed")
            return null
        }
        val lesson = LessonData (
            id = getNextLessonId(),
            title = newLessonData.title,
            resource = newLessonData.resource,
            duration = newLessonData.duration,
            sequenceNumber = newLessonData.sequenceNumber,
            status = newLessonData.status,
        )
        println("$CURRENT_FILE_NAME: New lesson created(id-${lesson.id})")
        // Add lesson id into module
        module.lessons.add(lesson)
        println("$CURRENT_FILE_NAME: New lesson attached to module(${moduleId})")
        return lesson
    }

    // ******************* UPDATE *******************
    override fun updateModuleDuration(moduleId: Int, duration: Int): Boolean {
        // Get Module
        val module = getModule(moduleId)
        if (module == null) {
            println("$CURRENT_FILE_NAME: Updating module duration failed")
            return false
        }
        // Update Duration
        module.duration += duration
        return true
    }

    override fun updateCourseDuration(courseId: Int, duration: Int): Boolean {
        // Get Course
        val course = getCourse(courseId)
        if (course == null) {
            println("$CURRENT_FILE_NAME: Updating course duration failed")
            return false
        }
        // Update Duration
        course.duration += duration
        return true
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        return categoryRecords.values.any { it -> it.name == name }
    }

    fun isCourseIdExists(courseId: Int): Boolean = courseId in courseRecords.keys

    // ******************* DELETE *******************

    // ******************* HELPER *******************
}
