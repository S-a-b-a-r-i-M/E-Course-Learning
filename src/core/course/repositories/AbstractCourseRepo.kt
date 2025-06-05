package core.course.repositories

import core.course.schemas.*
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.PriceDetails
import java.util.UUID


interface AbstractCourseRepo {
    // ******************* CREATE *******************
    fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course
    fun createCategory(name: String): Category
    fun createPriceDetails(newPriceData: NewPriceData): PriceDetails

    // ******************* READ *********************
    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category>
    fun getCourse()

    // ******************* UPDATE *******************
    fun addPriceDetailsIdToCourse(courseId: Int, priceDetailsId: Int)

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    fun isCategoryExists(name: String): Boolean
}