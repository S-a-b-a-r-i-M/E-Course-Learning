package core.course.services

import core.course.repositories.AbstractStudentCourseRepo
import core.course.schemas.CourseEnrollment
import core.course.schemas.CourseType
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import core.course.schemas.StudentProgress
import db.CompletionStatus
import java.util.UUID

/* Student Course Service
 * This service will handle student course enrollment, student lesson progress
 */
class StudentCourseService (private val repo: AbstractStudentCourseRepo) {
    fun getEnrolledCourseIds(studentId: UUID): List<Int> = repo.getEnrolledCourseIds(studentId)

    fun enrollCourse(newEnrollment: NewEnrollment): CourseEnrollment? {
        // TODO: Based on course type split the enrollment
        val status = if (newEnrollment.courseType == CourseType.LIVE)
            EnrollmentStatus.NOT_ASSIGNED
        else
            EnrollmentStatus.ASSIGNED

        val enrollment: CourseEnrollment = repo.enrollCourse(newEnrollment, status)
        return enrollment
    }

    fun getStudentProgress(studentId: UUID, courseId: Int) =
        repo.getStudentCourseProgress(studentId, courseId)

    fun updateStudentProgress(
        courseId: Int,
        lessonId: Int,
        studentId: UUID,
        isCompleted: Boolean
    ): Boolean {
        //TODO: If necessary: check if the user already having any in progress lesson status in the course
        val status = if (isCompleted) CompletionStatus.COMPLETED else CompletionStatus.IN_PROGRESS
        return repo.updateOrCreateStudentProgress(courseId, lessonId, studentId, status)
    }

    companion object {
        val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName
    }
}

