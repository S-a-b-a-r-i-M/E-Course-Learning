import core.auth.services.AuthService
import core.course.repositories.CourseRepo
import core.course.repositories.StudentCourseRepo
import core.course.schemas.CategoryData
import core.course.schemas.CourseLevel
import core.course.schemas.CourseType
import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewEnrollment
import core.course.schemas.NewPaymentDetails
import core.course.schemas.NewPriceData
import core.course.schemas.PriceDetailsData
import core.course.schemas.ResourceStatus
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import core.course.services.CourseService
import core.course.services.StudentCourseService
import core.course.services.capitalize
import core.user.repositories.UserRepo
import core.user.schemas.BaseUser
import core.user.schemas.UserData
import core.user.schemas.UserRole
import db.CompletionStatus
import utils.CourseDisplayHelper
import utils.InputValidator
import utils.currencyMap
import utils.getListInput
import utils.hasPermission
import java.util.UUID
import kotlin.io.print
import kotlin.io.println

fun authFlow(): BaseUser? {
    while (true) {
        println("\n======== Auth Page =========")
        println("\nOption to choose ⬇️")
        println("0 -> Exit")
        println("1 -> Sign Up")
        println("2 -> Sign In")
        val userInput = readln().toInt()

        // When - Auth Flow
        when (userInput) {
            0 -> break // It will break the outer loop
            1 -> {
                val userData: BaseUser? = authService.signIn()
                if (userData == null)
                    println("login failed. Try again...")
                else {
                    println("login success")
                    return userData
                }
            }
            2 -> {
                val userData = authService.signUp()
                if (userData == null)
                    println("sign up failed. Try again...")
                else {
                    println("sign up success")
                    return userData as BaseUser
                }
            }
            else -> println("Invalid input. Try again")
        }
    }

    return null
}

fun editPriceDetails(currentUser: UserData, courseId: Int): Boolean {
    val priceDetailsData = courseService.getCoursePriceDetails(courseId) ?: return false
    val updatedPriceData = UpdatePriceDetailsData(id=priceDetailsData.id)
    var isModified = false

    while (true) {
        println("\n===== Edit Price Details =====")
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
                // Compare to existing data
                if (currencyCode != priceDetailsData.currencyCode) {
                    updatedPriceData.currencyCode = currencyCode
                    updatedPriceData.currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")
                    isModified = true
                    println("New currency code added")
                } else {
                    println("Currency unchanged")
                }
            }
            2 -> {
                print("Enter new amount: ")
                val amount = readln().trim().toDoubleOrNull()
                if (amount == null || amount < 0){
                    println("Invalid amount entered. Try again")
                    continue
                }
                // Compare to existing data
                if (amount != priceDetailsData.amount) {
                    updatedPriceData.amount = amount
                    isModified = true
                    println("New amount code added")
                } else {
                    println("Amount unchanged")
                }
            }
            3 -> {
                if (isModified) {
                    print("Warning: Changes wont get saved. Are you sure (y/n) ?")
                    if (readln().trim().lowercase() == "y")
                        return false
                } else
                    return false
            }
            4 -> {
                courseService.updateCoursePricing(currentUser, courseId, updatedPriceData)
                return true
            }
            else -> println("Invalid input")
        }
    }
}

fun editCoursePricing(currentUser: UserData, courseId: Int) {
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
        println("\n===== Edit Pricing =====")
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
                            courseService.updateCoursePricing(currentUser, courseId, null)
                            println("Course set to Free!")
                            fetchPriceDetails() // refetch
                        }

                        2 -> {
                            // Set price details if switching to paid
                            println("Setting up price details for paid course...")
                            val priceData = getPriceDetails()
                            courseService.updateCoursePricing(currentUser, courseId, priceData)
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
                    if (editPriceDetails(currentUser, courseId))
                        fetchPriceDetails() // Refetch
                } ?: run {
                    println("Course is in Free status.Price details not set.")
                }
            }
            else -> println("Invalid input")
        }
    }
}

fun editLesson(currentUser: UserData, lessonId: Int): Boolean {
    val existingLessonData = courseService.getLesson(lessonId) ?: return false
    val updateLessonData = UpdateLessonData()
    var isModified = false

    while (true) {
        println("\n===== Edit Lesson =====")
        CourseDisplayHelper.displayDetailedLesson(existingLessonData, true)
        println("\nOption to choose ⬇️")
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
                    isModified = true
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
                    isModified = true
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
                    isModified = true
                    println("Duration updated")
                } else {
                    println("Invalid duration - $newDuration. Try again.")
                }
            }
            4 -> {
                selectResourceStatus() {
                    updateLessonData.status = it
                    isModified = true
                    println("Status updated")
                }
            }
            5 -> {
                if (isModified) {
                    print("Warning: Changes wont get saved. Are you sure (y/n) ?")
                    if (readln().trim().lowercase() == "y")
                        return false
                } else
                    return false
            }
            // Save & Go Back
            6 -> {
                courseService.updateLessonDetails(
                    currentUser,
                    existingLessonData.id,
                    updateLessonData
                )
                return true
            }
            else -> println("Invalid option")
        }
    }
}

fun manageLessons(currentUser: UserData, courseId: Int, moduleId: Int) {
    fun fetchModule() = courseService.getModule(moduleId)
    var module: ModuleData = fetchModule() ?: return

    while (true) {
        // display lessons
        println("\n===== Manage Lessons =====")
        println("Option to choose ⬇️")
        println("0 -> Go Back")
        println("1 -> Add Lesson")
        if (module.lessons.isNotEmpty()) {
            println("2 -> Edit Lesson")
//            println("3 -> Delete Lesson")
        }
        print("Choose option: ")

        when (readln().toIntOrNull()) {
            // Go Back
            0 -> break
            // Add Lesson
            1 -> {
                val lesson = courseService.createLesson(
                    currentUser,
                    courseId,
                    module.id,
                    module.lessons.size + 1
                )
                if (lesson == null) continue

                println("New Lesson 👇")
                CourseDisplayHelper.displayDetailedLesson(lesson, true)
                module = fetchModule() ?: return // Refetch
            }
            // Edit Lesson
            2 -> {
                if (module.lessons.isEmpty()) {
                    println("Invalid option selected. Please try again.")
                    continue
                }

                print("Enter lesson serial number to edit:")
                val inputIdx = readln().toInt() - 1
                if (inputIdx < 0 || inputIdx > module.lessons.size) {
                    println("Invalid input, try again.")
                    continue
                }
                // Find the lesson
                val lessonData = module.lessons[inputIdx]
                if (editLesson(currentUser, lessonData.id))
                    module = fetchModule() ?: return
            }
            // Delete Lesson
            /*
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
            */
            else -> println("Invalid option selected. Please try again.")
        }
    }
}

fun editModuleDetails(currentUser: UserData, courseId: Int, moduleId: Int): Boolean {
    val module = courseService.getModule(moduleId) ?: return false
    val updateModuleData = UpdateModuleData()
    var isModified = false

    while (true) {
        println("\n===== Edit Module =====")
        CourseDisplayHelper.displayModule(module)
        println("\nOption to choose ⬇️")
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
                    isModified = true
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
                isModified = true
                println("New description added")
            }
            3 -> selectResourceStatus {
                    updateModuleData.status = it
                    isModified = true
                    println("New status added")
                }
            4 -> manageLessons(currentUser, courseId, module.id)
            // Discard & Go Back
            5 -> {
                if (isModified) {
                    print("Warning: Changes wont get saved. Are you sure (y/n) ?")
                    if (readln().trim().lowercase() == "y")
                        return false
                } else
                    return false
            }
            // Save & Go Back
            6 -> {
                courseService.updateModuleDetails(currentUser, module.id, updateModuleData)
                return true
            }
            else -> println("Invalid option, try again.")
        }
    }
}

fun editCourseBasicDetails(currentUser: UserData, courseData: DetailedCourseData): Boolean {
    val updateCourseData = UpdateCourseBasicData()
    var isModified = false

    while (true) {
        println("\n===== Edit Basic Details =====")
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
                if (newTitle.isNotEmpty() && newTitle != courseData.title) {
                    updateCourseData.title = newTitle
                    isModified = true
                    println("New title added ☑️")
                } else
                    println("Title unchanged")
            }
            // Description
            2 -> {
                println("Current: ${courseData.description}")
                print("Enter new Description (or press Enter to keep current): ")
                val newDescription = readln().trim()
                if (newDescription.isNotEmpty() && newDescription != courseData.description) {
                    updateCourseData.description = newDescription
                    isModified = true
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
                if (newSkills.isNotEmpty() && newSkills != courseData.skills) {
                    updateCourseData.skills = newSkills
                    isModified = true
                    println("New skills added.")
                } else
                    println("Skills unchanged.")
            }
            // Prerequisites
            4 -> {
                println("Current: ${courseData.prerequisites?.joinToString(", ") ?: "None"}")
                val newData = getListInput("Enter prerequisites (separate by comma, or press enter to skip): ", ",")

                if (newData.isNotEmpty() && newData != courseData.prerequisites) {
                    updateCourseData.prerequisites = newData
                    isModified = true
                    println("New prerequisites added")
                } else
                    println("Prerequisites unchanged.")
            }
            // Status
            5 -> {
                println("Current: ${courseData.status}")
                selectResourceStatus {
                    updateCourseData.status = it
                    isModified = true
                    println("New staus added.")
                }
            }
            // Discard & Go Back
            6 -> {
                if (isModified) {
                    print("Warning: Changes wont get saved. Are you sure (y/n) ?")
                    if (readln().trim().lowercase() == "y")
                        return false
                } else
                    return false
            }
            // Save & Go Back
            7 -> {
                courseService.updateCourseBasicDetails(
                    currentUser,
                    courseData.id,
                    updateCourseData
                )
                return true
            }
            else -> println("Invalid option selected. Please try again.")
        }
    }
}

fun enrollCourse(studentId: UUID, course: DetailedCourseData): Boolean {
    var paymentDetails: NewPaymentDetails? = null
    if (course.priceDetails != null)
        paymentDetails = NewPaymentDetails(
            currencyCode = course.priceDetails.currencyCode,
            amount = course.priceDetails.amount,
        )

    val enrollment = studentCourseService.enrollCourse(
        NewEnrollment(
            courseId = course.id,
            studentId = studentId,
            courseType = course.courseType,
            paymentDetails = paymentDetails
        )
    )

    return enrollment != null
}

fun openCourseInLearningMode(course: DetailedCourseData, currentUser: UserData) {
    if (currentUser.role != UserRole.STUDENT) {
        println("Not a Student")
        return
    }
    // Get Student Progress
    val progress = studentCourseService.getStudentProgress(currentUser.id, course.id)

    // Show Modules
    println("====== MODULES ======")
    if (course.modules.isEmpty()) {
        println("No modules available.")
        return
    } else
        for((index, module) in course.modules.withIndex()) {
            CourseDisplayHelper.displayModule(
                module,
                true,
                2,
                index + 1,
                true,
                recentLessonId = progress?.recentLessonId ?: -1,
                recentLessonStatus = progress?.status ?: CompletionStatus.NOT_STARTED
            )
            // Add spacing between modules
            if (index < course.modules.size - 1) println()
        }
    println(" ==================== ")

    // Ask for start
    println("Are you ready to ${if (progress != null) "resume" else "start"} learning (y/n) ? ")
    if (readln().trim().lowercase() == "y") {
        var recentModuleIndex = 0
        var recentLessonIndex = 0

        // Find current progress position
        if (progress != null) {
            outer@ for (indexM in course.modules.indices) {
                val module = course.modules[indexM]
                for (indexL in module.lessons.indices) {
                    if (module.lessons[indexL].id == progress.recentLessonId) {
                        if (progress.status == CompletionStatus.COMPLETED) {
                            if (indexL + 1 < module.lessons.size) {
                                recentModuleIndex = indexM
                                recentLessonIndex = indexL + 1
                            } else if (indexM + 1 < course.modules.size) {
                                recentModuleIndex = indexM + 1
                                recentLessonIndex = 0
                            } else { // Course Already Completed
                                println("This Course already successfully completed ✅")
                                return
                            }
                        } else {
                            recentModuleIndex = indexM
                            recentLessonIndex = indexL
                        }
                        break@outer
                    }
                }
            }
        }

        // Main learning loop
        for (i in recentModuleIndex until course.modules.size) {
            val module = course.modules[i]
            val startLessonIndex = if (i == recentModuleIndex) recentLessonIndex else 0

            println("\nMODULE ${i + 1}: ${module.title}")
            for (j in startLessonIndex until module.lessons.size) {
                val lesson = module.lessons[j]

                println("\nLESSON ${j + 1}: ${lesson.title}")
                // Display lesson content
                println(lesson.resource)

                // User interaction
                println("\n-".repeat(3))
                print("\nDo you want to learn next lesson (y/n) ? ")

                fun updateStudentProgress(isCompleted: Boolean) {
                    studentCourseService.updateStudentProgress(
                        course.id,
                        lesson.id,
                        currentUser.id,
                        isCompleted
                    )
                }

                // Update Student Progress
                if (readln().trim().lowercase() == "y") {
                        updateStudentProgress(true)
                        println("Lesson completed ☑️!")
                }
                else {
                    print("Have you completed the current lesson (y/n) ? ")
                    updateStudentProgress(readln().trim().lowercase() == "y")
                    return
                }
            }

            // Module completion message
            println("\n🎉 Congratulations! You've completed Module ${i + 1}: ${module.title}")
            if (i < course.modules.size - 1)
                println("🚀 Ready for the next module!")
        }

        // Course completion
        println("\n" + "🎊".repeat(20))
        println("🏆 CONGRATULATIONS! 🏆")
        println("You have successfully completed the entire course:")
        println("📚 ${course.title}")
        println("🌟 Your learning journey for this course is now complete!")

    } else {
        println("📚 No problem! You can start learning anytime.")
    }
}

fun listCourses(pageTitle: PageNames, currentUser: UserData, onlyAssociated: Boolean = false) {
    var searchQuery = ""
    var offset = 0
    val limit = 10
    var hasMore = false

    // Decide User Type
    var isAdmin = false
    var isStudent = false
    if (currentUser.role == UserRole.ADMIN) isAdmin = true
    else if (currentUser.role == UserRole.STUDENT) isStudent = true

    fun fetchCourses(): List<DetailedCourseData> {
        val courses = courseService.getCourses(searchQuery, offset, limit, currentUser, onlyAssociated)
        if (courses.isEmpty()) {
            println("-------------- No Course to display -------------")
            hasMore = false
            return courses
        }
        courses.forEach { CourseDisplayHelper.displayCourse(it) }
        hasMore = courses.size == limit
        return courses
    }
    // If no course available in initial fetch then return immediately
    if (fetchCourses().isEmpty())
        return

    while (true) {
        println("\n======== ${pageTitle.value} =========")
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
                if (isStudent && PageNames.MY_COURSES == pageTitle) {
                    // Check if the entered course id is enrolled or not
                    if (!studentCourseService.getEnrolledCourseIds(currentUser.id).contains(courseId)) {
                        println("The selected course id is not yet enrolled by you.")
                        println("aborting to previous menu...")
                        continue
                    }
                }
                var course = courseService.getCourse(courseId)
                if (course == null)
                    continue

                // If Admin Then Show Edit Option
                if (isAdmin) {
                    while (true) {
                        if (course == null)
                            continue
                        println()
                        CourseDisplayHelper.displayCourse(course, true)
                        println("\nOption to choose ⬇️")
                        println("0 -> Go Back")
                        println("1 -> Edit Basic Details")
                        println("2 -> Edit Course Pricing")
                        println("3 -> Add New Module")
                        if (course.modules.isNotEmpty())
                            println("4 -> Edit Module Details")

                        print("Enter your option: ")
                        var isRefetch = false
                        when (readln().toInt()) {
                            0 -> break
                            1 -> {
                                if (editCourseBasicDetails(currentUser, course))
                                    // Refetch course After edit
                                    isRefetch = true
                            }
                            2 -> {
                                editCoursePricing(currentUser, courseId)
                                // Refetch course After edit
                                isRefetch = true
                            }
                            3 -> {
                                val module = courseService.createModule(
                                    currentUser, courseId, course.modules.size + 1
                                ) ?: continue
                                println("New Module 👇")
                                CourseDisplayHelper.displayModule(module)
                                // Add Lessons to New Module
                                println("Let's add lessons to your module...")
                                var lessonSeqNum = 1
                                do {
                                    courseService.createLesson(
                                        currentUser,
                                        courseId,
                                        module.id,
                                        lessonSeqNum++
                                    )
                                    print("Do you wanna add another lesson (y/n) ?")
                                } while (readln().trim().lowercase() == "y")
                                // Refetch Course Data To Fetch Newly Created lessons
                                isRefetch = true
                            }
                            4 -> {
                                if (course.modules.isEmpty()) {
                                    println("Invalid option selected. Please try again.")
                                    continue
                                }
                                print("Enter module id to edit: ")
                                val inputModuleId = readln().toInt()
                                val module = course.modules.find { it.id == inputModuleId }
                                if (module == null)
                                    println("module not found")
                                else {
                                    if (editModuleDetails(currentUser, courseId, module.id))
                                        isRefetch = true // Refetch Course
                                }
                            }
                            else -> println("Invalid option selected. Please try again.")
                        }

                        if (isRefetch) {
                            course = courseService.getCourse(courseId)
//                            println("Course refetched...")
                        }
                    }
                }
                // If Student Then Show Enroll Option
                else if (isStudent) {
                    CourseDisplayHelper.displayCourse(course, true)
                    // Get already enrolled courses ids
                    val enrolledCourseIds = studentCourseService.getEnrolledCourseIds(currentUser.id)

                    if (enrolledCourseIds.contains(courseId))
                        println("Already Enrolled ✅")
                    else {
                        print("Are you ready to learn new skills by enrolling this course (y/n) ? ")
                        if (readln().trim().lowercase() != "y") {
                            println("📚 No problem! You can start enroll anytime.")
                            continue
                        }

                        val isEnrolled = enrollCourse(currentUser.id, course)
                        println("Course enrollment is ${if (isEnrolled) "success ✅ " else "failed ❌"}")
                        if (isEnrolled)
                            println("Course will be available in your 'My Course' page.")
                    }
                    print("Do you want to start learning (y/n) ? ")
                    if (readln().trim().lowercase() == "y")
                        openCourseInLearningMode(course, currentUser)
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

private fun getSelectedCategoryFromUser(): CategoryData {
    /* Steps:
      1. Show First 10.
          1.1. Select Categories
          1.2. Load More(based on limit and retrieved data)
          1.3. Search
               Go to step 1
               1.3.1. If no categories found then create and ask user to search again to get the id
     */
    println("\n----- Choose Course Category -----")
    var searchQuery = ""
    var offset = 0
    val limit = 10
    var categories: List<CategoryData> = courseService.getCategories(searchQuery, offset, limit)

    // Show 10 default categories
    CourseDisplayHelper.displayCategories(categories, searchQuery)

    while (true) {
        println("\nOption to choose ⬇️")
        println("1 -> Select Category")
        println("2 -> Search 🔍")
//            if (categories.size == limit) println("3 -> Load More ↻")
        print("Enter your option: ")
        val userInput = readln().toInt()

        when (userInput) {
            // Select
            1 -> {
                print("Enter a category name: ")
                val input = readln().trim()
                if (input.isEmpty()) {
                    println("Invalid input. Please try again.")
                    continue
                }

                val selectedCategory = categories.find { it.name.equals(input, true) }
                if (selectedCategory == null) {
                    println("Invalid category. Please try again.")
                    continue
                }
                return selectedCategory
            }
            // Search
            2 -> {
                print("Enter Search Query:")
                val newSearchQuery = readln().trim()
                if (newSearchQuery == searchQuery) { // If there is no change no need to refetch
                    println("Same search query - no changes made")
                    continue
                }

                searchQuery = newSearchQuery
                offset = 0 // Reset offset when searching
                categories = courseService.getCategories(searchQuery, offset, limit)
                CourseDisplayHelper.displayCategories(categories, searchQuery)
            }
            // Load More
            /*
            3 -> {
                if (categories.size < limit) {
                    println("No more categories to load")
                    continue
                }
                offset += limit
                categories = getCategories(searchQuery, offset, limit)
                displayCategories()
            }
            */
            else -> {
                println("invalid option selected. Please try again.")
            }
        }
    }
}

fun getBasicCourseDataFromUser(): NewCourseBasicData {
    println("---------- Course Creation Section ----------")
    print("Enter course title (min 3 char, max 50 char): ")
    val title = InputValidator.validateName(readln(), "Title", 3, 50)
    print("Enter course description (min 10 char): ")
    val description = InputValidator.validateName(readln(), "Description", 10, 50)

    // Skills & Prerequisites
    val skills = getListInput("Enter skills(separate by comma): ", ",")
    val prerequisites = getListInput(
        "Enter prerequisites (separate by comma, or press enter to skip): ",
        ","
    )

    // Course Level & Type
    print("Enter Course Level(${CourseLevel.entries.joinToString(", ") { it.name.capitalize() }}):")
    val courseLevel = readln().trim().let { CourseLevel.getFromStrValue(it) } // Reason for using let: Better readability and clarity
    print("Enter Course Type(${CourseType.entries.joinToString(", ") { 
        it.name.capitalize().replace("_", "-") 
    }}):")
    val courseType = readln().trim().let { CourseType.getFromStrValue(it) }

    // Free course check with Price details
    print("Is this a free course? (y/n): ")
    val isFreeInput = readln().trim().lowercase()
    val isFree = isFreeInput == "y"
    var priceData: NewPriceData? = null
    if (!isFree) {
        println("\n----- Enter Price Details -----")
        print("Enter currency code (${currencyMap.keys.joinToString(", ")}): ")
        val currencyCode = readln().trim().uppercase()
        val currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")

        print("Enter amount: ")
        val amount = readln().trim().toDoubleOrNull() ?: run {
            println("Invalid amount entered. Setting base price as 0(In edit mode you can change it)")
            0.0
        }
        priceData = NewPriceData(currencyCode, currencySymbol, amount)
    }

    // Get Category
    val category = getSelectedCategoryFromUser()

    return NewCourseBasicData(
        title=title,
        description=description,
        skills=skills,
        courseLevel=courseLevel,
        courseType=courseType,
        category=category.name,
        prerequisites=prerequisites,
        priceData = priceData,
    )
}

fun createCourse(currentUser: UserData, newCourseData: NewCourseBasicData): DetailedCourseData? {
    // Create course with basic details
    val course = courseService.createCourse(currentUser, newCourseData)

    if (course == null) {
        return null
    }

    // Module & Lesson Creation
    do {
        val module = courseService.createModule(currentUser, course.id, course.modules.size + 1)
        if (module != null) {
            do {
                courseService.createLesson(currentUser, course.id, module.id, module.lessons.size + 1)
                print("Do you want to create another lesson(y/n) ?")
                val addAnotherLesson = readln().lowercase() == "y"
            } while (addAnotherLesson)
        }
        print("Do you want to create another module(y/n) ?")
        val addAnotherModule = readln().lowercase() == "y"
    } while (addAnotherModule)

    println("Course successfully created!!!")
    return course
}

fun homePageFlow(currentUser: UserData) {
    val isAdmin = currentUser.role == UserRole.ADMIN
    val isStudent = currentUser.role == UserRole.STUDENT

    while (true) {
        println("\n======== ${PageNames.HOME_PAGE.value} =========")
        println("\nOption to choose ⬇️")
        println("0 -> Back")
        println("1 -> List Of Courses")
        val option2 = if(isAdmin) PageNames.CREATE_COURSE else PageNames.MY_COURSES
        println("2 -> ${option2.value}")
        print("Enter your option: ")

        // When - Course Flow
        when (readln().toIntOrNull()) {
            0 -> break

            1 -> listCourses(PageNames.LIST_COURSES, currentUser)

            2 -> if(isAdmin) {
                // Create Course
                val newCourseData = getBasicCourseDataFromUser()
                val course = createCourse(currentUser, newCourseData)
                if (course != null) {
                    println("Do you wanna open the course(y/n) ?")
                    val openCourse = readln().trim().lowercase() == "y"
                    if (openCourse) {
                        CourseDisplayHelper.displayCourse(course, true)
                    }
                }
            } else if (isStudent) {
                // My Courses
                listCourses(option2, currentUser, true)
            }
        }
    }
}

fun main() {
    println("Welcome to grate kirigalan's show...")

    // App Flow
    val user: BaseUser? = authFlow()
    if (user == null) {
        println("User Login Error")
        return
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

    homePageFlow(UserData(
        user.id, user.firstName, user.lastName, user.email,
        user.role, user.status, user.hashPassword, user.lastLoginAt
    ))

    println("Welcome..visit again 😊")
}

// Object Creation
val userRepo = UserRepo()
val authService = AuthService(userRepo)
val courseRepo = CourseRepo()
val studentCourseRepo = StudentCourseRepo()
val studentCourseService = StudentCourseService(studentCourseRepo)
val courseService = CourseService(courseRepo, studentCourseService)

// Utility Function
fun selectResourceStatus(onSelected: (ResourceStatus) -> Unit) {
    println("Enter status (${ResourceStatus.entries.joinToString(", ") {it.name.capitalize()}}")
    val input = readln().trim()
    if (input.isNotEmpty())
        onSelected(ResourceStatus.getFromStrValue(input))
    else
        println("Status unchanged")
}

// ENUM Class
enum class PageNames(val value: String) {
    HOME_PAGE("Home Page"),
    AUTH_FLOW("Auth Flow"),
    CREATE_COURSE("Create Course"),
    LIST_COURSES("List Of Courses"),
    MY_COURSES("My Courses"),
    USER_MANAGEMENT("User Management"),
}