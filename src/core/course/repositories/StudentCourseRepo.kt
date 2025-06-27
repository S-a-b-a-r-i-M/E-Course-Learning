package core.course.repositories

import core.course.schemas.CourseEnrollment
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import core.course.schemas.PaymentDetails
import core.course.schemas.StudentProgress
import core.course.schemas.CompletionStatus
import java.util.UUID

class StudentCourseRepo : AbstractStudentCourseRepo {
    companion object {
        // Serial Ids
        private var courseEnrollmentId = 1
        private var paymentId = 1
        private var studentProgressId = 1

        private fun getNextCourseEnrollmentId() = courseEnrollmentId++
        private fun getNextPaymentId() = paymentId++
        private fun getNextStudentProgressId() = studentProgressId++

        // Storage
        private val courseEnrollmentRecords = mutableMapOf<Int, CourseEnrollment>()
        private val studentIdToEnrollmentIds = mutableMapOf<UUID, MutableList<Int>>()
        private val studentProgressRecords = mutableMapOf<Int, StudentProgress>()
        private val studentWithCourseToProgressId = mutableMapOf<Pair<UUID, Int>, Int>()
    }

    // ******************* CREATE *******************
    override fun createCourseEnrollment(newEnrollment: NewEnrollment, status: EnrollmentStatus): CourseEnrollment {
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

        return enrollment
    }

    // ******************* READ *********************
    override fun getEnrolledCourseIds(studentId: UUID): List<Int> =
        studentIdToEnrollmentIds[studentId]?.map { enrollmentId ->
            courseEnrollmentRecords.getValue(enrollmentId).courseId
        } ?: emptyList()

    override fun getCourseEnrollments(studentId: UUID): List<CourseEnrollment> =
        studentIdToEnrollmentIds[studentId]?.map { enrollmentId ->
            courseEnrollmentRecords.getValue(enrollmentId)
        } ?: emptyList()

    override fun getStudentCourseProgress(studentId: UUID, courseId: Int): StudentProgress? {
        val progressId = studentWithCourseToProgressId[Pair(studentId, courseId)] ?: return null
        return studentProgressRecords.getValue(progressId)
    }

    // ******************* UPDATE *******************
    override fun updateOrCreateStudentProgress(
        courseId: Int,
        lessonId: Int,
        studentId: UUID,
        status: CompletionStatus
    ): Boolean {
        val progressRecordId = studentWithCourseToProgressId[Pair(studentId, courseId)]

        if (progressRecordId == null) {
            // New Progress
            val progress = StudentProgress(
                id = getNextStudentProgressId(),
                studentId = studentId,
                recentLessonId = lessonId,
                courseId = courseId,
                status = status
            )
            studentProgressRecords[progress.id] = progress
            studentWithCourseToProgressId[Pair(studentId, courseId)] = progress.id
        } else {
            // Update Progress
            val progressRecord = studentProgressRecords.getValue(progressRecordId)
            studentProgressRecords[progressRecordId] = progressRecord.copy(
                recentLessonId = lessonId,
                status = status,
            )
        }

        return true
    }
}