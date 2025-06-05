package core.course.repositories

import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewPriceData
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.PriceDetails
import java.util.UUID


class CourseRepo : AbstractCourseRepo {
    // ******************* CREATE *******************
    override fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): Course {
        return Course.createCourse(newCourseData, currentUserId)
    }

    override fun createCategory(name: String): Category {
        return Category.createCategory(name)
    }

    override fun createPriceDetails(newPriceData: NewPriceData): PriceDetails {
        return PriceDetails.createPriceDetails(newPriceData)
    }

    // ******************* READ *********************
    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        // TODO: Needs to implement searchQuery & offset, limit
        return Category.getRecords().values.toList()
    }

    override fun getCourse() {
        println(Course.getRecords())
    }

    // ******************* UPDATE *******************
    override fun addPriceDetailsIdToCourse(courseId: Int, priceDetailsId: Int) {
        Course.addPriceDetailsIds(courseId, priceDetailsId)
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        return Category.getRecords().values.any { it -> it.name == name }
    }

    // ******************* DELETE *******************
}
