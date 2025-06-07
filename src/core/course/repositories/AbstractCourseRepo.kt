package core.course.repositories

import core.course.schemas.*
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.Module
import db.inmemorystore.course.PriceDetails
import java.util.UUID


interface AbstractCourseRepo {
    // ******************* CREATE *******************
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course
    fun createCategory(name: String): Category
    fun createPriceDetails(courseId: Int, newPriceData: NewPriceData): Boolean
    fun createModule(newModuleData: NewModuleData, courseId: Int): Module
    fun createLesson(newLessonData: NewLessonData, moduleId: Int): Lesson

    // ******************* READ *********************
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category>
    fun getPriceDetails(priceDetailsId: Int?): PriceDetails?
    fun getCourse(searchQuery: String, offset: Int, limit: Int): List<Course>

    // ******************* UPDATE *******************
    fun updateModuleDuration(moduleId: Int, duration: Float): Float
    fun updateCourseDuration(courseId: Int, duration: Float): Float

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
}