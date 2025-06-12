package core.course.services

import core.course.repositories.AbstractStudentCourseRepo
import core.course.schemas.CourseEnrollment
import core.course.schemas.CourseType
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import java.util.UUID

/* Student Course Service
 * This service will handle student course enrollment, student lesson progress
 */
class StudentCourseService (val repo: AbstractStudentCourseRepo) {
//    fun getEnrolledCourses(studentId: UUID): List<EnrolledCourse> {
//        // TODO: Implement search and pagination(filter is optional)
//        // TODO: Implement for live-course
//        val enrollments: List<CourseEnrollment> = repo.getCourseEnrollments(studentId)
//        if (enrollments.isEmpty()) return emptyList()
//
//        val courseIds = enrollments.map { it.courseId }
//        val courses = courseService.getCoursesByIds(courseIds)
//        return courses.zip(enrollments) {course, enrollment ->
//            EnrolledCourse(course, enrollment)
//        }
//    }

    fun getEnrolledCourseIds(studentId: UUID): List<Int> = repo.getEnrolledCourseIds(studentId)

    fun enrollCourse(newEnrollment: NewEnrollment): CourseEnrollment? {
        // TODO: Based on course type split the enrollment
        val status = if (newEnrollment.courseType == CourseType.LIVE)
            EnrollmentStatus.ASSIGNED
        else
            EnrollmentStatus.NOT_ASSIGNED

        val enrollment: CourseEnrollment = repo.enrollCourse(newEnrollment, status)
        return enrollment
    }

    fun getStudentProgress(courseId: Int, studentId: Int) {
        TODO("Not yet")
    }

    fun updateStudentProgress(courseId: Int, studentId: Int, ) {
        TODO("Not yet")
    }

    companion object {
        val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName
    }
}

