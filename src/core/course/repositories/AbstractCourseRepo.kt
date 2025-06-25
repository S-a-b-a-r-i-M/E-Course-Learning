package core.course.repositories

import core.course.schemas.*
import utils.Result
import java.util.UUID

interface AbstractCourseRepo {
    // ******************* CREATE *******************
    fun createCategory(name: String): CategoryData
    fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData?
    fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData?
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData
    fun createPricing(newPriceData: NewPriceData, courseId: Int): PriceDetailsData

    // ******************* READ *********************
    fun getCategory(categoryId: Int): CategoryData?
    fun getCategoryByName(name: String): CategoryData?
    fun getModule(moduleId: Int): ModuleData?
    fun getLesson(lessonId: Int): LessonData?
    fun getCourse(courseId: Int): DetailedCourseData?
    fun getPriceDetails(courseId: Int): PriceDetailsData?
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData>
    fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        courseIds: List<Int>? = null
    ): List<DetailedCourseData>
    fun getCoursesByIds(courseIds: List<Int>): List<DetailedCourseData>

    // ******************* UPDATE *******************
    fun updateOrCreatePricing(priceDetails: UpdatePriceDetailsData?, courseId: Int): Result<Unit>
    fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Result<Unit>
    fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Result<Unit>
    fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData) : Result<Unit>
    fun updateModuleDuration(moduleId: Int, duration: Int) : Boolean
    fun updateCourseDuration(courseId: Int, duration: Int): Boolean

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
}