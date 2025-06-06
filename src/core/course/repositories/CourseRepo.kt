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

        // Apply filter & Pagination
        return categories.filter {
            it.name.contains(searchQuery, true)
        }.let { filteredRes ->
            filteredRes.subList(offset, if (endIndex > filteredRes.size) filteredRes.size else endIndex)
        }
    }

    override fun getCourse() {
        val courses = Course.getRecords()
        println(courses)
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

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        return Category.getRecords().values.any { it -> it.name == name }
    }

    // ******************* DELETE *******************
}
