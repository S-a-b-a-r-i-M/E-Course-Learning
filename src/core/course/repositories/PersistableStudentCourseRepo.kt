package core.course.repositories

import core.course.schemas.CourseEnrollment
import core.course.schemas.EnrollmentStatus
import core.course.schemas.NewEnrollment
import core.course.schemas.NewPaymentDetails
import core.course.schemas.PaymentDetails
import core.course.schemas.StudentProgress
import db.CompletionStatus
import db.DatabaseManager
import java.util.UUID

class PersistableStudentCourseRepo : AbstractStudentCourseRepo {
    private val conn = DatabaseManager.getDBConnection()

    // ******************* CREATE *******************
    override fun enrollCourse(newEnrollment: NewEnrollment, status: EnrollmentStatus): CourseEnrollment {
        val sql = """
            INSERT INTO course_enrollment(course_id, student_id, status) 
            VALUES (?, ?, ?::EnrollmentStatus) 
            RETURNING id
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, newEnrollment.courseId)
            pstmt.setObject(2, newEnrollment.studentId)
            pstmt.setString(3, status.name)

            pstmt.executeQuery().use { rs ->
                rs.next()
                val enrollmentId = rs.getInt("id")
                // If Payment details present then create it.
                var paymentDetails: PaymentDetails? = null
                if (newEnrollment.paymentDetails != null)
                    paymentDetails = createPaymentDetails(
                        enrollmentId, newEnrollment.paymentDetails
                    )

                return CourseEnrollment(
                    id = enrollmentId,
                    courseId = newEnrollment.courseId,
                    studentId = newEnrollment.studentId,
                    status = status,
                    paymentDetails = paymentDetails
                )
            }
        }
    }

    private fun createPaymentDetails(
        enrollmentId: Int,
        newPaymentData: NewPaymentDetails
    ): PaymentDetails {
        val sql = """
            INSERT INTO payment_details(enrollment_id, currencycode, amount) 
            VALUES (?, ?, ?) 
            RETURNING id
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, enrollmentId)
            pstmt.setString(2, newPaymentData.currencyCode)
            pstmt.setDouble(3, newPaymentData.amount)

            pstmt.executeQuery().use { rs ->
                rs.next()
                return PaymentDetails(
                    id = rs.getInt("id"),
                    currencyCode = newPaymentData.currencyCode,
                    amount = newPaymentData.amount
                )
            }
        }
    }

    // ******************* READ *********************
    override fun getEnrolledCourseIds(studentId: UUID): List<Int> {
        val sql = "SELECT course_id FROM course_enrollment WHERE student_id='$studentId'"
        val courseIds = mutableListOf<Int>()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next())
                    courseIds.add(rs.getInt("course_id"))

                return courseIds
            }
        }
    }

    override fun getCourseEnrollments(studentId: UUID): List<CourseEnrollment> {
        val sql = """
            SELECT c.*, p.id as p_id, p.enrollment_id, p.currencycode, p.amount
            FROM course_enrollment c
            LEFT JOIN payment_details p ON c.id = p.enrollment_id
            WHERE student_id='$studentId'
        """.trimIndent()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                val enrollments = mutableListOf<CourseEnrollment>()
                while (rs.next()) {
                    enrollments.add (
                        CourseEnrollment(
                            id = rs.getInt("id"),
                            courseId = rs.getInt("course_id"),
                            studentId = rs.getObject("student_id", UUID::class.java),
                            status = EnrollmentStatus.getFromString(rs.getString("status")),
                            paymentDetails = PaymentDetails(
                                id = rs.getInt("p_id"),
                                currencyCode = rs.getString("currencyCode"),
                                amount = rs.getDouble("amount")
                            )
                        )
                    )
                }

                return enrollments
            }
        }
    }

    override fun getStudentCourseProgress(studentId: UUID, courseId: Int): StudentProgress? {
        val sql = "SELECT * FROM student_lesson_progress WHERE student_id='$studentId' AND course_id=$courseId"

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next())
                    return StudentProgress(
                        id = rs.getInt("id"),
                        studentId = rs.getObject("student_id", UUID::class.java),
                        recentLessonId = rs.getInt("lesson_id"),
                        courseId = rs.getInt("course_id"),
                        status = CompletionStatus.getFromString(rs.getString("status"))
                    )

                return null
            }
        }
    }

    // ******************* UPDATE *******************
    override fun updateOrCreateStudentProgress(
        courseId: Int,
        lessonId: Int,
        studentId: UUID,
        status: CompletionStatus
    ): Boolean {
        val sql = """
            INSERT INTO student_lesson_progress (course_id, lesson_id, student_id, status) 
            VALUES (?, ?, ?, ?::CompletionStatus)
            ON CONFLICT (course_id, student_id)
            DO UPDATE SET
                lesson_id = EXCLUDED.lesson_id,
                status = EXCLUDED.status;
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            // Add Values
            pstmt.setInt(1, courseId)
            pstmt.setInt(2, lessonId)
            pstmt.setObject(3, studentId)
            pstmt.setString(4, status.name)

            // Execute
            val count = pstmt.executeUpdate()
            return count != 0
        }
    }
}