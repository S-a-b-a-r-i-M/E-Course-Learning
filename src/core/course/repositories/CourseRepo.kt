package core.course.repositories

import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.Module
import db.inmemorystore.course.PriceDetails
import java.util.UUID


class CourseRepo : AbstractCourseRepo {
    // ******************* READ *********************
    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        val endIndex = (offset + 1) * limit
        val categories = Category.getRecords().values.toList()

        // Apply Search
        var result = categories.filter { it.getName().contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getPriceDetails(priceDetailsId: Int?): PriceDetails? {
        return PriceDetails.getRecords()[priceDetailsId]
    }

    override fun getCourses(searchQuery: String, offset: Int, limit: Int): List<Course> {
        val endIndex = (offset + 1) * limit
        val courses = Course.getRecords().values.toList()

        // Apply Search
        var result  = courses.filter { it.getTitle().contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourse(courseId: Int): Course? {
        return Course.getRecords()[courseId]
    }

    override fun getModules(moduleIds: List<Int>): List<Module> {
        val moduleIdsSet = moduleIds.toSet() // For fast look up
        return Module.getRecords().values.filter { moduleIdsSet.contains(it.getId()) }
    }

    override fun getLessons(lessonIds: List<Int>): List<Lesson> {
        val lessonIdsSet = lessonIds.toSet()
        return Lesson.getRecords().values.filter { lessonIdsSet.contains(it.getId()) }
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
        Course.updatePriceDetailsId(courseId, priceDetails.getId())
        return true
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): Module {
        val module = Module.createModule(newModuleData)
        // Add module id into course
        Course.addModuleId(courseId, module.getId())
        return module
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): Lesson {
        val lesson = Lesson.createLesson(newLessonData)
        // Add lesson id into module
        Module.addLessonId(moduleId, lesson.getId())
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
        return Category.getRecords().values.any { it -> it.getName() == name }
    }

    // ******************* DELETE *******************
}
