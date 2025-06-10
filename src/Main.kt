import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.PriceDetailsData
import core.course.schemas.ResourceStatus
import core.course.services.CourseDisplayService
import core.course.services.CourseService
import core.course.services.capitalize
import core.course.services.currencyMap
import core.user.repositories.UserRepo
import core.user.schemas.UserData
import core.user.schemas.UserRole
import core.user.schemas.UserStatus
import utils.getListInput
import java.time.LocalDateTime
import java.util.UUID

fun authFlow(authService: AuthService): UserData? {
    while (true) {
        println("\nOption to choose ⬇️")
        println("0 -> Exit")
        println("1 -> Sign In")
        println("2 -> Sign Up")
        val userInput = readln().toInt()

        // When - Auth Flow
        when (userInput) {
            0 -> break // It will break the outer loop
            1 -> {
                val userData: UserData? = authService.signIn()
                if (userData == null)
                    println("login failed. Try again...")
                else {
                    println("login success")
                    return userData
                }
            }
            2 -> {
                val userData: UserData? = authService.signUp()
                if (userData == null)
                    println("sign up failed. Try again...")
                else {
                    println("sign up success")
                    return userData
                }
            }
            else -> println("Invalid input. Try again")
        }
    }

    return null
}

fun editCourseBasicDetails(courseData: DetailedCourseData) {
    val duplicateCourseData = courseData.copy()
    while (true) {
        println("\n=== Edit Basic Details ===")
//        println("Current Course Details:")
//        CourseDisplayService.displayCourse(courseData, true)

        println("\nWhat would you like to edit?")
        println("1 -> Title")
        println("2 -> Description")
        println("3 -> Skills")
        println("4 -> Free/Paid Status")
        println("5 -> Price Details")
        println("6 -> Prerequisites")
        println("7 -> Status")
        println("8 -> Discard & Go Back")
        println("9 -> Save & Go Back")

        print("Enter your choice: ")

        when (readln().toInt()) {
            // Title
            1 -> {
                println("Current Title: ${courseData.title}")
                print("Enter new title (or press Enter to keep current): ")
                val newTitle = readln().trim()
                if (newTitle.isNotEmpty())
                    duplicateCourseData.title = newTitle
            }
            // Description
            2 -> {
                println("Current Description: ${courseData.description}")
                print("Enter new Description (or press Enter to keep current): ")
                val newDescription = readln().trim()
                if (newDescription.isNotEmpty())
                    duplicateCourseData.description = newDescription
            }
            // Skills
            3 -> {
                println("Current skills: ${courseData.skills.joinToString(", ")}")
                val newSkills = getListInput(
                    "Enter skills(separate by comma) or press enter to keep old skills: ",
                    ","
                )
                if (newSkills.isNotEmpty())
                    duplicateCourseData.skills = newSkills
            }
            // Free/Paid Status
            4 -> {
                println("Current status: ${if (duplicateCourseData.isFreeCourse) "Free" else "Paid"}")
                println("1 -> Free Course")
                println("2 -> Paid Course")
                print("Select option (or press Enter to keep current): ")
                val input = readln().trim()
                if (input.isNotEmpty()) {
                    when (input.toIntOrNull()) {
                        1 -> {
                            duplicateCourseData.isFreeCourse = true
                            duplicateCourseData.priceDetails = null // Clear price details for free courses
                            println("✅ Course set to Free!")
                        }
                        2 -> {
                            courseData.isFreeCourse = false
                            if (courseData.priceDetails == null) {
                                // Set default price details if switching to paid
                                println("Setting up price details for paid course...")
                                println("Current price details:")
                                courseData.priceDetails?.let {
                                    println("Price: ${it.currencySymbol}${it.amount} (${it.currencyCode})")
                                } ?: println("No price details set")

                                print("Enter currency code (${currencyMap.keys.joinToString(", ")}): ")
                                val currencyCode = readln().trim().uppercase()
                                val currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")
                                print("Enter amount: ")
                                val amount = readln().trim().toDoubleOrNull()

                                if (amount == null)
                                    println("Invalid amount format.")
                                else if (amount <= 0)
                                    println("Amount has to greater than 0.")
                                else {
                                    duplicateCourseData.priceDetails = PriceDetailsData (
                                        id = duplicateCourseData.priceDetails?.id ?: 0, // Keep existing ID or set to 0 for new
                                        currencyCode = currencyCode,
                                        currencySymbol = currencySymbol,
                                        amount = amount
                                    )
                                }
                            }
                            println("✅ Course set to Paid!")
                        }
                        else -> println("Invalid choice.")
                    }
                } else {
                    println("Free/Paid status unchanged.")
                }
            }
            // Price Details
            5 -> {}
            // Prerequisites
            6 -> {
                println("Current prerequisites: ${courseData.prerequisites?.joinToString(", ") ?: "None"}")
                println(
                    "Enter prerequisites separated by commas (or press Enter to keep current):"
                )
                val input = readln().trim()
                when {
                    input.isEmpty() -> println("Prerequisites unchanged.")
                    input.lowercase() == "none" -> {
                        courseData.prerequisites = null
                        println("Prerequisites cleared!")
                    }
                    else -> {
                        val newData = getListInput("Enter prerequisites (separate by comma, or press enter to skip): ", ",")
                        if (newData.isNotEmpty())
                            duplicateCourseData.prerequisites = newData
                    }
                }
            }
            // Status
            7 -> {
                println("Current status: ${courseData.status}")
                println("Enter Course Status(${ResourceStatus.entries.joinToString(", ") { it.name.capitalize() }}) or press enter to keep current:")
                val status = readln().trim()
                if (status.isNotEmpty()) {
                    duplicateCourseData.status = ResourceStatus.getFromStrValue(status)
                }
            }
            // Discard & Go Back
            8 -> return
            // Save & Go Back
            9 -> {
                // Save Edited details and Return
            }
            else -> println("Invalid option selected. Please try again.")
        }
    }
}

fun manageLessons(module: ModuleData) {
    while (true) {
//        displayLessonsList(module.lessons)

        println("\n=== Manage Lessons ===")
        println("0 -> Go Back")
        println("1 -> Add Lesson")
        if (module.lessons.isNotEmpty()) {
            println("2 -> Edit Lesson")
            println("3 -> Delete Lesson")
        }
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            0 -> break
            1 -> CourseService().createLesson()
            2 -> if (module.lessons.isNotEmpty()) {}
            3 -> if (module.lessons.isNotEmpty()) { }
            else -> println("Invalid option")
        }
    }
}

fun editModuleDetails(module: ModuleData) {
    while (true) {
//        displayModuleDetails(module)

        println("\n=== Edit Module ===")
        println("1 -> Edit Title")
        println("2 -> Edit Description")
        println("3 -> Edit Status")
        println("4 -> Manage Lessons")
        println("0 -> Go Back")
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            0 -> break
            1 -> {
                println("Current: ${module.title}")
                print("New title: ")
                val newTitle = readln().trim()
                if (newTitle.isNotEmpty())
                    module.title = newTitle
            }
            2 -> {
                println("Current: ${module.description ?: "None"}")
                print("New description: ")
                val input = readln().trim()
                module.description = input.ifEmpty { null }
            }
            3 -> {

            }
            4 -> manageLessons(module)
            else -> println("❌ Invalid option")
        }
    }
}

fun listCourses(courseService: CourseService, currentUser: UserData) {
    var searchQuery = ""
    var offset = 0
    val limit = 10
    var hasMore = false

    fun fetchCourses() {
        val courses = courseService.getCourses(searchQuery, offset, limit)
        if (courses.isEmpty()) {
            println("-------------- No Course to display -------------")
            hasMore = false
            return
        }
        courses.forEach { CourseDisplayService.displayCourse(it) }
        hasMore = courses.size == limit
    }
    fetchCourses()

    while (true) {
        println("\nOption to choose ⬇️")
        println("0 -> Go Back")
        println("1 -> Open a course")
        println("2 -> Search by Course name 🔍")
        if (hasMore) println("3 -> Load More ↻")
        print("Enter your option: ")
        val userInput = readln().toInt()

        when (userInput) {
            // Go Back
            0 -> return
            // Open a Course
            1 -> {
                print("Enter course id: ")
                val courseId = readln().toInt()
                val detailedCourseData = courseService.getCourse(courseId)
                if (detailedCourseData == null)
                    continue
                CourseDisplayService.displayCourse(detailedCourseData, true)
                // If Admin User Then Show Edit Option
                if (currentUser.role == UserRole.ADMIN) {
                    while (true) {
                        println("\nOption to choose ⬇️")
                        println("0 -> Go Back")
                        println("1 -> Edit Basic Details")
                        println("2 -> Add Module")
                        if (detailedCourseData.modules.isNotEmpty())
                            println("3 -> Edit Module Details")

                        when (readln().toInt()) {
                            0 -> break
                            1 -> editCourseBasicDetails(detailedCourseData)
                            2 -> {}
                            3 -> {
                                print("Select module to edit (by id): ")
                                val moduleId = readln().toInt()
                                val module = detailedCourseData.modules.find { it.id == moduleId }
                                if (module == null)
                                    println("module not found")
                                else
                                    editModuleDetails(module)
                            }
                            else -> println("Invalid option selected. Please try again.")
                        }
                    }
                }
            }
            // Search
            2 -> {
                print("Enter Search Query: ")
                val newSearchQuery = readln().trim()
                if (newSearchQuery == searchQuery) { // If there is no change no need to refetch
                    println("Same search query - no changes made")
                    continue
                }

                searchQuery = newSearchQuery
                offset = 0 // Reset offset when searching
                fetchCourses()
            }
            // Load More
            3 -> {
                if (!hasMore) {
                    println("No more courses to load")
                    continue
                }
                offset += limit
                fetchCourses()
            }
            else -> {
                println("invalid option selected. Please try again.")
            }
        }
    }
}

fun courseFlow(courseService: CourseService, currentUser: UserData) {
    while (true) {
        println("\nOption to choose ⬇️")
        println("0 -> Back")
        println("1 -> List Of Courses")
        println("2 -> Create Course")
        val userInput = readln().toInt()

        // When - Course Flow
        when (userInput) {
            0 -> return // It will break the outer loop
            1 -> listCourses(courseService, currentUser)
            2 -> {
                val course = courseService.createCourse(currentUser)
                if (course != null) {
                    println("Do you wanna open the course(y/n) ?")
                    val openCourse = readln().trim().lowercase() == "y"
                    if (openCourse)
                        CourseDisplayService.displayCourse(course, true)
                }
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
//    val user: UserData? = authFlow(authService)
//    if (user == null) return
    /**/
    val user = UserData(
        id = UUID.randomUUID(),
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
        role = UserRole.ADMIN,
        hashPassword = "newUserData.hashedPassword",
        status = UserStatus.ACTIVE,
        lastLoginAt = LocalDateTime.now()
    )


    // Object Creation
    val courseRepo = CourseRepo()
    val courseService = CourseService(courseRepo)

    courseFlow(courseService, user)

    println("Welcome..visit again 😊")
}