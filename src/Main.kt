import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.repositories.PersistableCourseRepo
import core.course.repositories.PersistableStudentCourseRepo
import core.course.repositories.StudentCourseRepo
import core.course.services.CourseService
import core.course.services.StudentCourseService
import core.user.repositories.PersistableUserRepo
import core.user.repositories.UserRepo
import core.user.schemas.BaseUser
import core.user.schemas.UserData
import db.DatabaseManager
import pages.AuthPage
import pages.HomePage
import kotlin.io.println

const val isPersistableStorage = true

fun main() {
    while (true) {
        // App Flow
        val user: BaseUser? = authPage.authFlow()
        if (user == null) {
            break
        }

        homePage.start(
            UserData(
                user.id, user.firstName, user.lastName, user.email,
                user.role, user.status, user.hashPassword, user.lastLoginAt
            )
        )
    }

    // Close the DB connection
    if (isPersistableStorage)
        DatabaseManager.closeConnection()

    println("Welcome..visit again 😊")
}

// Object Creation
val userRepo = if (isPersistableStorage) PersistableUserRepo() else UserRepo()
val authService = AuthService(userRepo)
val authPage = AuthPage(authService)

val studentCourseRepo = if (isPersistableStorage) PersistableStudentCourseRepo() else StudentCourseRepo()
val studentCourseService = StudentCourseService(studentCourseRepo)

val courseRepo = if (isPersistableStorage) PersistableCourseRepo() else CourseRepo()
val courseService = CourseService(courseRepo, studentCourseService)

val homePage = HomePage(courseService, studentCourseService)