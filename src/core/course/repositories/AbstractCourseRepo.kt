package core.course.repositories

import core.course.schemas.*
import java.util.UUID


interface AbstractCourseRepo {
    // ******************* CREATE *******************
    fun createCategory(name: String): CategoryData
    fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData?
    fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData
    fun createPriceDetails(newPriceData: NewPriceData, courseId: Int): PriceDetailsData?

    // ******************* READ *********************
    fun getCourse(courseId: Int): DetailedCourseData?
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData>
    fun getCourses(searchQuery: String, offset: Int, limit: Int): List<DetailedCourseData>

    // ******************* UPDATE *******************
    fun updateModuleDuration(moduleId: Int, duration: Int): Boolean
    fun updateCourseDuration(courseId: Int, duration: Int): Boolean

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
    fun getCategory(id: Int): CategoryData
}