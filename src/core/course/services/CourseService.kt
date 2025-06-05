package core.course.services

import core.course.repositories.AbstractCourseRepo
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewPriceData
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.PriceDetails
import java.util.UUID

class CourseService (val courseRepo: AbstractCourseRepo) {
    fun getCourse() {
        courseRepo.getCourse()
    }

    fun createCourse(courseData: NewCourseBasicData, currentUserId: UUID): Course {
        val course = courseRepo.createCourse(courseData, currentUserId)

        // Attach PriceDetails To Course
        if (courseData.priceData != null) {
            val priceDetails = courseRepo.createPriceDetails(courseData.priceData)
            courseRepo.addPriceDetailsIdToCourse(course.id, priceDetails.id)
        }

        return course
    }

    fun addPriceDetailsToCourse(courseId: Int, newPriceData: NewPriceData): PriceDetails {
        val priceDetails = courseRepo.createPriceDetails(newPriceData)
        println("$newPriceData is price details created.")
        return priceDetails
    }

    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        return courseRepo.getCategories(searchQuery, offset, limit)
    }

    fun createCategory(name: String): Category {
        val category = courseRepo.createCategory(name)
        println("New Category created $category")
        return category
    }
}