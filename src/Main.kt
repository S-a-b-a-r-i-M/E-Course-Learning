import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.PriceDetailsData
import core.course.schemas.ResourceStatus
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
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
import kotlin.io.print
import kotlin.io.println

val courseRepo = CourseRepo()
val courseService = CourseService(courseRepo)

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

fun editPriceDetails(courseId: Int): Boolean {
    val priceDetailsData = courseService.getCoursePriceDetails(courseId)
    if (priceDetailsData == null) return false

    val updatedPriceData = UpdatePriceDetailsData(id=priceDetailsData.id)
    while (true) {
        println("\n=== Edit Price Details ===")
        println("What would you like to edit?")
        println("1 -> Currency Code")
        println("2 -> Amount")
        println("3 -> Discard & Go Back")
        println("4 -> Save & Go Back")

        print("Enter your choice: ")
        when(readln().toIntOrNull()) {
            1 -> {
                print("Enter new currency code (${currencyMap.keys.joinToString(", ")}): ")
                val currencyCode = readln().trim().uppercase()
                if (currencyCode !in currencyMap.keys) {
                    println("Invalid currency code. Try again.")
                    continue
                }
                updatedPriceData.currencyCode = currencyCode
                updatedPriceData.currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")
            }
            2 -> {
                print("Enter new amount: ")
                val amount = readln().trim().toDoubleOrNull()
                if (amount == null || amount < 0){
                    println("Invalid amount entered. Try again")
                    continue
                }
                updatedPriceData.amount = amount
            }
            3 -> {
                return false
            }
            4 -> {
                courseService.updateCoursePricing(courseId, updatedPriceData)
                return true
            }
            else -> println("Invalid input")
        }
    }
}

fun editCoursePricing(courseId: Int) {
    fun getPriceDetails(): UpdatePriceDetailsData {
        print("Enter currency code (${currencyMap.keys.joinToString(", ")}): ")
        val currencyCode = readln().trim().uppercase()
        val currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")
        print("Enter amount: ")
        val amount = readln().trim().toDoubleOrNull() ?: run {
            println("Invalid amount entered. Setting base price as 1")
            1.0
        }
        return UpdatePriceDetailsData(0, currencyCode, currencySymbol, amount)
    }

    var priceDetails: PriceDetailsData? = null
    fun fetchPriceDetails() {
        priceDetails = courseService.getCoursePriceDetails(courseId)
    }
    fetchPriceDetails() // Initial fetch

    while (true) {
        val isFreeCourse = priceDetails != null
        println("\n=== Edit Pricing ===")
        println("What would you like to edit?")
        println("0 -> Go Back")
        println("1 -> Free/Paid Status")
        if (isFreeCourse)
            println("2 -> Price Details")

        print("Enter your choice: ")
        when (readln().toIntOrNull()) {
            0 -> break
            1 -> {
                println("Current status: ${if (isFreeCourse) "Free" else "Paid"}")
                println("1 -> Free Course")
                println("2 -> Paid Course")
                print("Select option (or press Enter to keep current): ")
                val input = readln().trim()
                if (input.isNotEmpty()) {
                    when (input.toIntOrNull()) {
                        1 -> {
                            // Set course as free & remove price details
                            courseService.updateCoursePricing(courseId, null)
                            println("Course set to Free!")
                            fetchPriceDetails() // refetch
                        }

                        2 -> {
                            // Set price details if switching to paid
                            println("Setting up price details for paid course...")
                            val priceData = getPriceDetails()
                            courseService.updateCoursePricing(courseId, priceData)
                            println("Course set to Paid!")
                            fetchPriceDetails() // refetch
                        }
                        else -> println("Invalid choice.")
                    }
                } else {
                    println("Free/Paid status unchanged.")
                }
            }
            2 -> {
                priceDetails?.let {
                    println("Existing Price: ${it.currencySymbol}${it.amount} (${it.currencyCode})")
                    if (editPriceDetails(courseId))
                        fetchPriceDetails() // Refetch
                } ?: run {
                    println("Course is in Free status.Price details not set.")
                }
            }
            else -> println("Invalid input")
        }
    }
}

fun editLesson(lessonId: Int): Boolean {
    val existingLessonData = courseService.getLesson(lessonId)
    if (existingLessonData == null) return false
    val updateLessonData = UpdateLessonData()

    while (true) {
        println("\n=== Edit Lesson ===")
        println("1 -> Edit Title")
        println("2 -> Edit Resource")
        println("3 -> Edit Duration")
        println("4 -> Edit Status")
        println("5 -> Discard & Go Back")
        println("6 -> Save & Go Back")
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            1 -> {
                println("Current: ${updateLessonData.title ?: existingLessonData.title}")
                print("New title (or press Enter to keep current): ")
                val newTitle = readln().trim()
                if (newTitle.isNotEmpty()) {
                    updateLessonData.title = newTitle
                    println("New Title added")
                } else
                    println("Title unchanged")
            }
            2 -> {
                println("Current: ${updateLessonData.resource ?: existingLessonData.resource}")
                print("New resource (or press Enter to keep current): ")
                val newResource = readln().trim()
                if (newResource.isNotEmpty()) {
                    updateLessonData.resource = newResource
                    println("New Resource added")
                } else
                    println("Resource unchanged")
            }
            3 -> {
                println("Current: ${updateLessonData.duration ?: existingLessonData.duration} minutes")
                print("New duration (or press Enter to keep current): ")
                val newDuration = readln().toIntOrNull()
                if (newDuration != null && newDuration > 0) {
                    updateLessonData.duration = newDuration
                    println("Duration updated")
                } else {
                    println("Invalid duration - $newDuration. Try again.")
                }
            }
            4 -> {
                selectResourceStatus() {
                    updateLessonData.status = it
                    println("Status updated")
                }
            }
            5 -> {
                println("Warning: Changes wont get saved. Are you sure (y/n) ?")
                if (readln().trim().lowercase() == "y") return false
            }
            // Save & Go Back
            6 -> {
                courseService.updateLessonDetails(existingLessonData.id, updateLessonData)
                return true
            }
            else -> println("Invalid option")
        }
    }
}

fun manageLessons(courseId: Int, moduleId: Int) {
    fun fetchModule() = courseService.getModule(moduleId)
    var module: ModuleData = fetchModule() ?: return

    while (true) {
        // display lessons
        println("\n=== Manage Lessons ===")
        println("0 -> Go Back")
        println("1 -> Add Lesson")
        if (module.lessons.isNotEmpty()) {
            println("2 -> Edit Lesson")
            println("3 -> Delete Lesson")
        }
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            // Go Back
            0 -> break
            // Add Lesson
            1 -> {
                val lesson = courseService.createLesson(
                    courseId, module.id, module.lessons.size + 1
                )
                println("New Lesson 👇")
                CourseDisplayService.displayDetailedLesson(lesson, true)
                module = fetchModule() ?: return // Refetch
            }
            // Edit Lesson
            2 -> {
                if (module.lessons.isEmpty()) {
                    println("Invalid option selected. Please try again.")
                    continue
                }

                print("Enter lesson id to edit:")
                val inputId = readln().toIntOrNull()
                if (inputId == null) {
                    println("Invalid input, try again.")
                    continue
                }
                // Find the lesson
                val lessonData = module.lessons.find { it.id == inputId }
                if (lessonData == null) {
                    println("For given id($inputId) no lesson founded, try again.")
                    continue
                }

                if (editLesson(lessonData.id))
                    module = fetchModule() ?: return
            }
            // Delete Lesson
            3 -> {
                if (module.lessons.isEmpty()) {
                    println("Invalid option selected. Please try again.")
                    continue
                }

                println("Enter lesson id to delete: ")
                readln().toIntOrNull().let { inputId ->
                    if (inputId == null)
                        println("Invalid Lesson id.")
                    else {
                        val isDeleted = courseService.deleteLesson(inputId)
                        println("Selected lesson deletion result is $isDeleted")
                        module = fetchModule() ?: return
                    }
                }
            }
            else -> println("Invalid option selected. Please try again.")
        }
    }
}

fun editModuleDetails(courseId: Int, moduleId: Int): Boolean {
    val module = courseService.getModule(moduleId)
    if (module == null) return false
    val updateModuleData = UpdateModuleData()

    while (true) {
//        displayModuleDetails(module)
        println("\n=== Edit Module ===")
        println("1 -> Edit Title")
        println("2 -> Edit Description")
        println("3 -> Edit Status")
        println("4 -> Manage Lessons")
        println("5 -> Discard & Go Back")
        println("6 -> Save & Go Back")
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            1 -> {
                println("Current: ${updateModuleData.title ?: module.title}")
                print("Enter New title (or press Enter to keep current): ")
                val newTitle = readln().trim()
                if (newTitle.isNotEmpty()) {
                    updateModuleData.title = newTitle
                    println("New title added")
                }
                else
                    println("Title unchanged.")
            }
            2 -> {
                println("Current: ${updateModuleData.description ?: module.description ?: "None"}")
                print("Enter New description: ")
                val input = readln().trim()
                updateModuleData.description = input.ifEmpty { null }
                println("New description added")
            }
            3 -> selectResourceStatus {
                    updateModuleData.status = it
                    println("New status added")
                }
            4 -> manageLessons(courseId, module.id)
            // Discard & Go Back
            5 -> {
                println("Warning: Changes wont get saved. Are you sure (y/n) ?")
                if (readln().trim().lowercase() == "y") return false
            }
            // Save & Go Back
            6 -> {
                courseService.updateModuleDetails(module.id, updateModuleData)
                return true
            }
            else -> println("Invalid option, try again.")
        }
    }
}

fun editCourseBasicDetails(courseData: DetailedCourseData): Boolean {
    val updateCourseData = UpdateCourseBasicData()
    while (true) {
        println("\n=== Edit Basic Details ===")
        println("What would you like to edit?")
        println("1 -> Title")
        println("2 -> Description")
        println("3 -> Skills")
        println("4 -> Prerequisites")
        println("5 -> Status")
        println("6 -> Discard & Go Back")
        println("7 -> Save & Go Back")

        print("Enter your choice: ")

        when (readln().toInt()) {
            // Title
            1 -> {
                println("Current: ${courseData.title}")
                print("Enter new title (or press Enter to keep current): ")
                val newTitle = readln().trim()
                if (newTitle.isNotEmpty()) {
                    updateCourseData.title = newTitle
                    println("New title added ☑️")
                } else
                    println("Title unchanged")
            }
            // Description
            2 -> {
                println("Current: ${courseData.description}")
                print("Enter new Description (or press Enter to keep current): ")
                val newDescription = readln().trim()
                if (newDescription.isNotEmpty()) {
                    updateCourseData.description = newDescription
                    println("New description added.")
                } else
                    println("Description unchanged.")
            }
            // Skills
            3 -> {
                println("Current: ${courseData.skills.joinToString(", ")}")
                val newSkills = getListInput(
                    "Enter skills(separate by comma) or press enter to keep old skills: ",
                    ","
                )
                if (newSkills.isNotEmpty()) {
                    updateCourseData.skills = newSkills
                    println("New skills added.")
                } else
                    println("Skills unchanged.")
            }
            // Prerequisites
            4 -> {
                println("Current: ${courseData.prerequisites?.joinToString(", ") ?: "None"}")
                val newData = getListInput("Enter prerequisites (separate by comma, or press enter to skip): ", ",")

                if (newData.isNotEmpty()) {
                    println("New prerequisites added")
                    updateCourseData.prerequisites = newData
                } else
                    println("Prerequisites unchanged.")
            }
            // Status
            5 -> {
                println("Current: ${courseData.status}")
                selectResourceStatus {
                    updateCourseData.status = it
                    println("New staus added.")
                }
            }
            // Discard & Go Back
            6 -> {
                print("Warning: Changes wont get saved. Are you sure (y/n) ?")
                if (readln().trim().lowercase() == "y")
                    return false
            }
            // Save & Go Back
            7 -> {
                courseService.updateCourseBasicDetails(courseData.id, updateCourseData)
                return true
            }
            else -> println("Invalid option selected. Please try again.")
        }
    }
}

fun listCourses(currentUser: UserData) {
    var searchQuery = ""
    var offset = 0
    val limit = 10
    var hasMore = false

    fun fetchCourses() {
        val courses = courseService.getCourses(searchQuery, offset, limit, currentUser)
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
            0 -> break
            // Open a Course
            1 -> {
                print("Enter course id: ")
                val courseId = readln().toInt()
                var courseData = courseService.getCourse(courseId)
                if (courseData == null)
                    continue
                CourseDisplayService.displayCourse(courseData, true)
                // If Admin Then Show Edit Option
                if (currentUser.role == UserRole.ADMIN) {
                    while (true) {
                        if (courseData == null)
                            continue
                        println("\nOption to choose ⬇️")
                        println("0 -> Go Back")
                        println("1 -> Edit Basic Details")
                        println("2 -> Edit Course Pricing")
                        println("3 -> Add New Module")
                        if (courseData.modules.isNotEmpty())
                            println("4 -> Edit Module Details")

                        print("Enter your option: ")
                        var isRefetch = false
                        when (readln().toInt()) {
                            0 -> break
                            1 -> {
                                if (editCourseBasicDetails(courseData))
                                    // Refetch course After edit
                                    isRefetch = true
                            }
                            2 -> {
                                editCoursePricing(courseId)
                                // Refetch course After edit
                                isRefetch = true
                            }
                            3 -> {
                                val module = courseService.createModule(
                                    courseId, courseData.modules.size + 1
                                )
                                println("New Module 👇")
                                CourseDisplayService.displayModule(module)
                                // Add Lessons to New Module
                                println("Let's add lessons to your module...")
                                var lessonSeqNum = 1
                                do {
                                    courseService.createLesson(courseId, module.id, lessonSeqNum++)
                                    print("Do you wanna add lessons to your lesson (y/n) ?")
                                } while (readln().trim().lowercase() == "y")
                                // Refetch Course Data To Fetch Newly Created lessons
                                isRefetch = true
                            }
                            4 -> {
                                if (courseData.modules.isEmpty()) {
                                    println("Invalid option selected. Please try again.")
                                    continue
                                }
                                print("Enter module id to edit: ") // TODO: Change this to module id
                                val inputModuleId = readln().toInt()
                                val module = courseData.modules.find { it.id == inputModuleId }
                                if (module == null)
                                    println("module not found")
                                else {
                                    if (editModuleDetails(courseId, module.id))
                                        isRefetch = true// Refetch Course
                                }
                            }
                            else -> println("Invalid option selected. Please try again.")
                        }

                        if (isRefetch) {
                            courseData = courseService.getCourse(courseId)
                            println("Course refetched...")
                        }
                    }
                }
                // If Student Then Show Enroll Option
                else if (currentUser.role == UserRole.STUDENT) {

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
    val isAdmin = currentUser.role == UserRole.ADMIN
    val isStudent = currentUser.role == UserRole.STUDENT
    while (true) {
        println("\nOption to choose ⬇️")
        println("0 -> Back")
        println("1 -> List Of Courses")
        println("2 -> ${if(isAdmin) "Create Course" else "My Courses"}")

        val userInput = readln().toInt()

        // When - Course Flow
        when (userInput) {
            0 -> return // It will break the outer loop
            1 -> listCourses(currentUser)
            2 -> if(isAdmin) {
                // Create Course
                val course = courseService.createCourse(currentUser)
                if (course != null) {
                    println("Do you wanna open the course(y/n) ?")
                    val openCourse = readln().trim().lowercase() == "y"
                    if (openCourse)
                        CourseDisplayService.displayCourse(course, true)
                }
            } else if (isStudent) {
                // My Courses

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
    courseFlow(courseService, user)

    println("Welcome..visit again 😊")
}

// Utility Function
fun selectResourceStatus(onSelected: (ResourceStatus) -> Unit) {
    println("Enter status (${ResourceStatus.entries.joinToString(", ") {it.name.capitalize()}}")
    val input = readln().trim()
    if (input.isNotEmpty())
        onSelected(ResourceStatus.getFromStrValue(input))
    else
        println("Status unchanged")
}