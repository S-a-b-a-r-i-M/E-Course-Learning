package core.course.schemas
import db.CompletionStatus
import java.util.UUID

data class PaymentDetails(
    val id: Int,
    val currencyCode: String,
    val amount: Double
)

data class NewPaymentDetails(
    val currencyCode: String,
    val amount: Double
)

data class CourseEnrollment (
    val id: Int,
    val courseId: Int,
    val studentId: UUID,
    val status: EnrollmentStatus,
    val paymentDetails: PaymentDetails? = null
)

data class EnrolledCourse (
    val course: DetailedCourseData,
    val enrollmentData: CourseEnrollment,
)

data class NewEnrollment (
    val courseId: Int,
    val studentId: UUID,
    val courseType: CourseType,
    val paymentDetails: NewPaymentDetails?
)

data class StudentLessonProgress (
    val id: Int,
    val studentId: UUID,
    val recentLessonId: Int,
    val courseId: Int,
    var status: CompletionStatus,
//    var completedDateTime: LocalDateTime? = null
)