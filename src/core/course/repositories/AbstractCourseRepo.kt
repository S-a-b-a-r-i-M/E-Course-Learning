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
    fun createCategory(name: String): Category
    fun createLesson(newLessonData: NewLessonData, moduleId: Int): Lesson
    fun createModule(newModuleData: NewModuleData, courseId: Int): Module
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course
    fun createPriceDetails(courseId: Int, newPriceData: NewPriceData): Boolean

    // ******************* READ *********************
    fun getCourse(courseId: Int): Course?
    fun getModules(moduleIds: List<Int>): List<Module>
    fun getLessons(lessonIds: List<Int>): List<Lesson>
    fun getPriceDetails(priceDetailsId: Int?): PriceDetails?
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category>
    fun getCourses(searchQuery: String, offset: Int, limit: Int): List<Course>

    // ******************* UPDATE *******************
    fun updateModuleDuration(moduleId: Int, duration: Float): Float
    fun updateCourseDuration(courseId: Int, duration: Float): Float

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
}