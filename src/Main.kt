import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.repositories.StudentCourseRepo
import core.course.services.CourseService
import core.course.services.StudentCourseService
import core.user.repositories.UserRepo
import core.user.schemas.BaseUser
import core.user.schemas.UserData
import pages.AuthPage
import pages.HomePage
import kotlin.io.println


fun main() {
    while (true) {
        // App Flow
        val user: BaseUser? = authPage.authFlow()
        if (user == null) {
            break
        }

//    val user = UserData(
//        id = UUID.randomUUID(),
//        firstName = "Sathrabathi",
//        lastName = "Sivaji",
//        email = "sivaji@gmail.com",
//        role = UserRole.ADMIN,
//        hashPassword = "PasswordHasher.getHashPassword(password)",
//        status = UserStatus.ACTIVE,
//        lastLoginAt = LocalDateTime.now()
//    )

        homePage.start(
            UserData(
                user.id, user.firstName, user.lastName, user.email,
                user.role, user.status, user.hashPassword, user.lastLoginAt
            )
        )
    }

    println("Welcome..visit again 😊")
}

// Object Creation
val userRepo = UserRepo()
val authService = AuthService(userRepo)
val authPage = AuthPage(authService)

val studentCourseRepo = StudentCourseRepo()
val studentCourseService = StudentCourseService(studentCourseRepo)

val courseRepo = CourseRepo()
val courseService = CourseService(courseRepo, studentCourseService)

val homePage = HomePage(courseService, studentCourseService)

// ENUM Class
enum class PageNames(val value: String) {
    HOME_PAGE("Home Page"),
    AUTH_FLOW("Auth Flow"),
    CREATE_COURSE("Create Course"),
    LIST_COURSES("List Of Courses"),
    MY_COURSES("My Courses"),
    USER_MANAGEMENT("User Management"),
}