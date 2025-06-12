package core.course.repositories

import core.course.schemas.CourseEnrollment
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import java.util.UUID

interface AbstractStudentCourseRepo {
    // ******************* CREATE *******************
    fun enrollCourse(newEnrollment: NewEnrollment, status: EnrollmentStatus): CourseEnrollment

    // ******************* READ *********************
    fun getEnrolledCourseIds(studentId: UUID): List<Int>
    fun getCourseEnrollments(studentId: java.util.UUID): List<CourseEnrollment>

    // ******************* UPDATE *******************

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
}