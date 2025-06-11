package core.course.repositories

import core.course.schemas.*
import java.util.UUID

interface AbstractCourseRepo {
    // ******************* CREATE *******************
    fun createCategory(name: String): CategoryData
    fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData
    fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData
    fun createPricing(newPriceData: NewPriceData, courseId: Int): PriceDetailsData

    // ******************* READ *********************
    fun getCategory(categoryId: Int): CategoryData?
    fun getModule(moduleId: Int): ModuleData?
    fun getLesson(lessonId: Int): LessonData?
    fun getCourse(courseId: Int): DetailedCourseData?
    fun getPriceDetails(courseId: Int): PriceDetailsData?
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData>
    fun getCourses(searchQuery: String, offset: Int, limit: Int): List<DetailedCourseData>

    // ******************* UPDATE *******************
    fun updateModuleDuration(moduleId: Int, duration: Int): Boolean
    fun updateCourseDuration(courseId: Int, duration: Int): Boolean
    fun updateOrCreatePricing(priceDetails: PriceDetailsData?, courseId: Int): Boolean
    fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Boolean
    fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Boolean
    fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData) : Boolean

    // ******************* DELETE *******************
    fun deleteLesson(lessonId: Int): Boolean

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
}