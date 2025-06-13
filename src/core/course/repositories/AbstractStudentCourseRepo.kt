package core.course.repositories

import core.course.schemas.CourseEnrollment
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import core.course.schemas.StudentProgress
import db.CompletionStatus
import java.util.UUID

interface AbstractStudentCourseRepo {
    // ******************* CREATE *******************
    fun enrollCourse(newEnrollment: NewEnrollment, status: EnrollmentStatus): CourseEnrollment

    // ******************* READ *********************
    fun getEnrolledCourseIds(studentId: UUID): List<Int>
    fun getCourseEnrollments(studentId: UUID): List<CourseEnrollment>
    fun getStudentCourseProgress(studentId: UUID, courseId: Int): StudentProgress?

    // ******************* UPDATE *******************
    fun updateOrCreateStudentProgress(
        courseId: Int,
        lessonId: Int,
        studentId: UUID,
        status: CompletionStatus
    ): Boolean

    // ******************* DELETE *******************

    // ******************* EXISTS *******************

}