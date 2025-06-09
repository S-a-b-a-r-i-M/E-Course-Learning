package core.course.repositories

import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.schemas.PriceDetailsData
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.Module
import db.inmemorystore.course.PriceDetails
import java.util.UUID


class CourseRepo : AbstractCourseRepo {
    // ******************* READ *********************
    override fun getCategory(id: Int): Category {
        return Category.getRecords().getValue(id)
    }

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        val endIndex = (offset + 1) * limit
        val categories = Category.getRecords().values.toList()

        // Apply Search
        var result = categories.filter { it.name.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getPriceDetails(priceDetailsId: Int): PriceDetailsData {
        return PriceDetails.getRecords().getValue(priceDetailsId).let {
            PriceDetailsData(
                id = it.id,
                currencyCode = it.getCurrencyCode(),
                currencySymbol = it.getCurrencySymbol(),
                amount = it.getAmount()
            )
        }
    }

    override fun getCourses(searchQuery: String, offset: Int, limit: Int): List<Course> {
        val endIndex = (offset + 1) * limit
        val courses = Course.getRecords().values.toList()

        // Apply Search
        var result  = courses.filter { it.getTitle().contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourse(courseId: Int): DetailedCourseData? {
        val course = Course.getRecords()[courseId] ?: return null
        val detailedCourseData = DetailedCourseData(
            id = course.id,
            createdBy = course.createdBy,
            category = getCategory(course.categoryId).name,
            title = course.getTitle(),
            description = course.getDescription(),
            duration = course.getDuration(),
            skills = course.getSkills(),
            courseLevel = course.courseLevel,
            courseType = course.courseType,
            isFreeCourse = course.isFreeCourse(),
            status = course.getStatus(),
            prerequisites = course.getPrerequisites(),
        )

        // Get Price Details
        val priceDetailsId = course.getPriceDetailsId()
        if (priceDetailsId != null) {
            detailedCourseData.priceDetails = getPriceDetails(priceDetailsId)

        // Get Modules
        val moduleIds = course.getModuleIds()
        if (moduleIds.isEmpty()) return detailedCourseData
        val modules: List<Module> = getModules(moduleIds)

        // Get Lessons
        detailedCourseData.modules = modules.filter {
            it.getLessonIds().isNotEmpty()
        }.map {
            val lessonIds = it.getLessonIds()
            val lessons: List<Lesson> = courseRepo.getLessons(lessonIds)
            ModuleData (it, lessons)
        }

        return detailedCourseData
    }

    override fun getModules(moduleIds: List<Int>): List<Module> {
        val moduleIdsSet = moduleIds.toSet() // For fast look up
        return Module.getRecords().values.filter { moduleIdsSet.contains(it.id) }
    }

    override fun getLessons(lessonIds: List<Int>): List<Lesson> {
        val lessonIdsSet = lessonIds.toSet()
        return Lesson.getRecords().values.filter { lessonIdsSet.contains(it.id) }
    }

    // ******************* CREATE *******************
    override fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course {
        return Course.createCourse(newCourseData, currentUserId)
    }

    override fun createCategory(name: String): Category {
        return Category.createCategory(name)
    }

    override fun createPriceDetails(courseId: Int, newPriceData: NewPriceData): Boolean {
        val priceDetails = PriceDetails.createPriceDetails(newPriceData)
        // Add price-details id into course
        Course.updatePriceDetailsId(courseId, priceDetails.id)
        return true
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): Module {
        val module = Module.createModule(newModuleData)
        // Add module id into course
        Course.addModuleId(courseId, module.id)
        return module
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): Lesson {
        val lesson = Lesson.createLesson(newLessonData)
        // Add lesson id into module
        Module.addLessonId(moduleId, lesson.id)
        return lesson
    }

    // ******************* UPDATE *******************
    override fun updateModuleDuration(moduleId: Int, duration: Float): Float {
        return Module.updateDuration(moduleId, duration)
    }

    override fun updateCourseDuration(courseId: Int, duration: Float): Float {
        return Course.updateDuration(courseId, duration)
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        return Category.getRecords().values.any { it -> it.name == name }
    }

    // ******************* DELETE *******************

    // ******************* HELPER *******************
//    private fun convertCourseToDetailedCourseData(course: Course) = DetailedCourseData(
//
//    )
}
