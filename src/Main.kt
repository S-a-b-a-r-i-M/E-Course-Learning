import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewPriceData
import core.course.services.CourseService
import core.user.repositories.UserRepo
import db.CourseLevel
import db.CourseType
import db.UserRole
import db.UserStatus
import db.inmemorystore.course.Category
import db.inmemorystore.user.User
import utils.getListInput
import java.time.LocalDateTime
import java.util.Currency
import java.util.UUID

fun authFlow(authService: AuthService): User? {
    while (true) {
        println("\nOption to choose ⬇️")
        println("1 -> Sign In")
        println("2 -> Sign Up")
        println("0 -> Exit")
        val userInput = readln().toInt()

        // When - Auth Flow
        when (userInput) {
            0 -> break // It will break the outer loop
            1 -> {
                val user: User? = authService.signIn()
                if (user == null)
                    println("login failed. Try again...")
                else {
                    println("login success")
                    return user
                }
            }
            2 -> {
                val user = authService.signUp()
                if (user == null)
                    println("sign up failed. Try again...")
                else {
                    println("sign up success")
                    return user
                }
            }
            else -> println("Invalid input. Try again")
        }
    }

    return null
}

fun courseFlow(courseService: CourseService, currentUserId: UUID) {
    while (true) {
        println("\nOption to choose ⬇️")
        println("1 -> List Of Courses")
        println("2 -> Create Course")
        println("0 -> Exit")
        val userInput = readln().toInt()

        // When - Course Flow
        when (userInput) {
            0 -> break // It will break the outer loop
            1 -> courseService.getCourse()
            2 -> {
                val course = courseService.createCourse(currentUserId)
            }
        }
    }
}

fun main() {
    println("Welcome to grate kirigalan's show...")
    // Object Creation
    val userRepo = UserRepo()
    val authService = AuthService(userRepo)

    // App Flow
//    val user: User? = authFlow(authService)
//    if (user == null) return
    /**/
    val user = User(
        id = UUID.randomUUID(),
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
        role = UserRole.STUDENT,
        hashPassword = "newUserData.hashedPassword",
        status = UserStatus.ACTIVE,
        lastLoginAt = LocalDateTime.now()
    )


    // Object Creation
    val courseRepo = CourseRepo()
    val courseService = CourseService(courseRepo)

//    courseFlow(courseService, user.id)
      println(courseService.getSelectedCategoryIdFromUser())

    println("Welcome..visit again 😊")
}