package core.course.services

import core.course.repositories.AbstractStudentCourseRepo
import core.course.schemas.CourseEnrollment
import core.course.schemas.CourseType
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import db.CompletionStatus
import java.util.UUID

/* Student Course Service
 * This service will handle student course enrollment, student lesson progress
 */
class StudentCourseService (private val repo: AbstractStudentCourseRepo) {
    /**
     * Retrieves the IDs of all courses a specific student is enrolled in.
     *
     * @param studentId The unique identifier of the student.
     * @return A list of integer course IDs.
     */
    fun getEnrolledCourseIds(studentId: UUID): List<Int> = repo.getEnrolledCourseIds(studentId)

    /**
     * Enrolls a student in a course.
     *
     * @param newEnrollment An object containing the student and course details.
     * @return The created [CourseEnrollment] object.
     */
    fun enrollCourse(newEnrollment: NewEnrollment): CourseEnrollment? {
        val status = if (newEnrollment.courseType == CourseType.LIVE)
            EnrollmentStatus.NOT_ASSIGNED
        else
            EnrollmentStatus.ASSIGNED

        val enrollment: CourseEnrollment = repo.createCourseEnrollment(newEnrollment, status)
        return enrollment
    }

    /**
     * Retrieves the progress details for a student in a given course.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return A data object representing the student's progress.
     */
    fun getStudentProgress(studentId: UUID, courseId: Int) =
        repo.getStudentCourseProgress(studentId, courseId)

    /**
     * Updates a student's progress for a single lesson.
     *
     * This will create the progress record if it does not already exist, otherwise it'll update.
     *
     * @param courseId The ID of the course containing the lesson.
     * @param lessonId The ID of the lesson to update.
     * @param studentId The ID of the student.
     * @param isCompleted `true` to mark the lesson as completed, `false` for in-progress.
     * @return `true` if the update was successful, `false` otherwise.
     */
    fun updateStudentProgress(
        courseId: Int,
        lessonId: Int,
        studentId: UUID,
        isCompleted: Boolean
    ): Boolean {
        val status = if (isCompleted) CompletionStatus.COMPLETED else CompletionStatus.IN_PROGRESS
        return repo.updateOrCreateStudentProgress(courseId, lessonId, studentId, status)
    }
}

