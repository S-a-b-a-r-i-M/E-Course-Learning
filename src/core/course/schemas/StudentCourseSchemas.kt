package core.course.schemas
import java.util.UUID

data class PaymentDetails (
    val id: Int,
    val currencyCode: String,
    val amount: Double
)

data class NewPaymentDetails (
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

data class NewEnrollment (
    val courseId: Int,
    val studentId: UUID,
    val courseType: CourseType,
    val paymentDetails: NewPaymentDetails?
)

data class StudentProgress (
    val id: Int,
    val studentId: UUID,
    val recentLessonId: Int,
    val courseId: Int,
    var status: CompletionStatus,
)