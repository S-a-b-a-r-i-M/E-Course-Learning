package core.course.repositories

import core.course.schemas.CourseEnrollment
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import core.course.schemas.PaymentDetails
import java.util.UUID

class StudentCourseRepo : AbstractStudentCourseRepo {
    companion object {
        // Serial Ids
        private var courseEnrollmentId = 1
        private var paymentId = 1

        private fun getNextCourseEnrollmentId() = courseEnrollmentId++
        private fun getNextPaymentId() = courseEnrollmentId++

        // Storage
        private val courseEnrollmentRecords = mutableMapOf<Int, CourseEnrollment>()
        private val studentIdToEnrollmentIds = mutableMapOf<UUID, MutableList<Int>>()
    }

    // ******************* CREATE *******************
    override fun enrollCourse(newEnrollment: NewEnrollment, status: EnrollmentStatus): CourseEnrollment {
        val enrollment = CourseEnrollment(
            id = getNextCourseEnrollmentId(),
            courseId = newEnrollment.courseId,
            studentId = newEnrollment.studentId,
            status = status,
            paymentDetails = newEnrollment.paymentDetails?.let {
                PaymentDetails(
                    id = getNextPaymentId(),
                    currencyCode = it.currencyCode,
                    amount = it.amount
                )
            }
        )

        courseEnrollmentRecords[enrollment.id] = enrollment
        if (enrollment.studentId !in studentIdToEnrollmentIds)
            studentIdToEnrollmentIds[enrollment.studentId] = mutableListOf(enrollment.id)
        else
            studentIdToEnrollmentIds[enrollment.studentId]?.add(enrollment.id)

        return enrollment.copy()
    }

    override fun getEnrolledCourseIds(studentId: UUID): List<Int> =
        studentIdToEnrollmentIds[studentId]?.map { enrollmentId ->
            courseEnrollmentRecords.getValue(enrollmentId).courseId
        } ?: emptyList()

    override fun getCourseEnrollments(studentId: UUID): List<CourseEnrollment> =
        studentIdToEnrollmentIds[studentId]?.map { enrollmentId ->
            courseEnrollmentRecords.getValue(enrollmentId).copy()
        } ?: emptyList()


    // ******************* READ *********************

    // ******************* UPDATE *******************

    // ******************* DELETE *******************

}