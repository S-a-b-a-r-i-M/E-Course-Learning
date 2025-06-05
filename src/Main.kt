import core.auth.schemas.SignInData
import core.auth.schemas.SignUpData
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
import java.time.LocalDateTime
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
                // Read Input
                println("Enter email : ")
                val email = readln().trim()
                println("Enter password : ")
                val password = readln().trim()

                val user: User? = authService.signIn(SignInData(email, password))

                if (user == null)
                    println("login failed. Try again...")
                else {
                    println("login success")
                    return user
                }
            }
            2 -> {
                // Read Input
                println("Enter first name : ")
                val firstName = readln().trim()
                println("Enter last name : ")
                val lastName = readln().trim()
                println("Enter email : ")
                val email = readln().trim()
                println("Enter password : ")
                val password1 = readln().trim()
                println("Enter password again : ")
                val password2 = readln().trim()

                if (password1 != password2) {
                    println("Password didn't match.")
                    continue
                }

                val user = authService.signUp(
                    SignUpData(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password1,
                    )
                )
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

fun getListInput(prompt: String, delimiter: String): List<String> {
    print(prompt)
    val input = readln().trim()
    return if (input.isNotEmpty())
        input.split(delimiter).map { it.trim() }.filter { it.isNotEmpty() }
    else
        emptyList()
}

fun getBasicCourseDataFromUser(): NewCourseBasicData {
    print("Enter course title: ")
    val title = readln().trim()

    print("Enter course description: ")
    val description = readln().trim()

    // Skills & Prerequisites
    val skills = getListInput("Enter skills(separated by comma[,]): ", ",")
    val prerequisites = getListInput(
        "Enter prerequisites (separated by comma[,] or press enter to skip)",
        ","
    )

    // Course Level
    println("Enter Course Level(${CourseLevel.entries.joinToString(", ")}):")
    val courseLevel = readln().trim().let { CourseLevel.getFromStrValue(it) } // Reason for using let: Better readability and clarity

    // Course Type
    println("Enter Course Type(${CourseType.entries.joinToString(", ")}):")
    val courseType = readln().trim().let { CourseType.getFromStrValue(it) }

    // Free course check with Price details
    print("Is this a free course? (y/n): ")
    val isFreeInput = readln().trim().lowercase()
    val isFree = isFreeInput == "y"
    var priceData: NewPriceData? = null
    if (!isFree) {
        println("=== Enter Price Details ===")
        print("Enter currency code (e.g., USD, EUR): ")
        val currencyCode = readln().trim().uppercase()

        print("Enter currency symbol (e.g., $, €): ")
        val currencySymbol = readln().trim()

        print("Enter amount:")
        val amount = readln().trim().toDoubleOrNull() ?: run {
            println("Invalid amount entered. Setting base price as 0")
            0.0
        }
        priceData = NewPriceData(currencyCode, currencySymbol, amount)
    }

    return NewCourseBasicData(
        title=title,
        description=description,
        skills=skills,
        courseLevel=courseLevel,
        courseType=courseType,
        prerequisites=prerequisites,
        isFreeCourse=isFree,
        priceData = priceData
    )
}

fun getSelectedCategoriesIds(categories: List<Category>): List<Int> {
    /* Steps:
      1. Take input from user
      2. Search In DB
      3. Show First 10. two options coming below
        3.1 -> click load more
         3.1.1 -> Show next 10. again go to step 3.1
        3.2 -> select
         3.2.1 -> Enter more than one id
      4. Attach the selected ids to the course
     */
    println("=== Choose Course Category ===")
    println(String.format("%-5s | %-20s", "ID", "Category"))
    println("-".repeat(30))
    categories.forEach{
        println(String.format("%-5d | %-20s", it.id, it.name))
    }

    val categoryIds = getListInput(
        "Enter category ids(comma separated(,)):",
        ","
    )
    // TODO: Validate all the ids
    return categoryIds.map { it.toInt() }
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
                // Basic
                val newCourseData = getBasicCourseDataFromUser()
                // Category
                val categories = courseService.getCategories("", 0, 100)
                newCourseData.categoryIds = getSelectedCategoriesIds(categories)
                val course = courseService.createCourse(newCourseData, currentUserId)
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
//    println("Retrieved User from auth flow : $user")
//    if (user == null) return
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

    courseFlow(courseService, user.id)

    println("Welcome..visit again 😊")
}