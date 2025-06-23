package pages

import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewEnrollment
import core.course.schemas.NewPaymentDetails
import core.course.services.CourseService
import core.course.services.StudentCourseService
import core.user.schemas.UserData
import core.user.schemas.UserRole
import db.CompletionStatus
import utils.displayCategories
import utils.displayCourse
import utils.displayModule
import utils.getNewCourseBasicDataFromUser
import utils.getNewLessonDataFromUser
import utils.getNewModuleDataFromUser
import utils.getYesOrNo
import utils.selectFromOption
import java.util.UUID

class CoursePage (
    val courseService: CourseService,
    val studentCourseService: StudentCourseService,
    val editCoursePage: EditCoursePage
) {
    fun getSelectedCategory(): CategoryData {
        println("\n----- Choose Course Category -----")
        var searchQuery = ""
        var offset = 0
        val limit = 10
        var categories: List<CategoryData> = courseService.getCategories(searchQuery, offset, limit)

        // Show 10 default categories
        displayCategories(categories, searchQuery)

        val options = mutableMapOf(1 to "Select Category", 2 to "Search 🔍")
//        if (categories.size == limit) options.put(3, "Load More ↻")
        while (true) {
            when (selectFromOption(options)) {
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
                    displayCategories(categories, searchQuery)
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
            }
        }
    }

    fun buildNewCourseData(): NewCourseBasicData {
        val newCourseBasicData = getNewCourseBasicDataFromUser()
        // Get Category
        newCourseBasicData.category = getSelectedCategory().name
        return newCourseBasicData
    }

    fun createCourse(currentUser: UserData): DetailedCourseData? {
        // Create course with basic details
        val newCourseData = buildNewCourseData()

        val course = courseService.createCourse(currentUser, newCourseData)
        if (course == null) {
            return null
        }

        // Module & Lesson Creation
        do {
            createModule(currentUser, course.id)
            val addAnotherModule = getYesOrNo("Do you want to create another module(y/n) ?")
        } while (addAnotherModule)

        println("Course successfully created!!!")

        if (getYesOrNo("Do you wanna open the course(y/n) ?")) {
            val detailedCourse = courseService.getCourse(course.id)
            if (detailedCourse != null) displayCourse(detailedCourse, true)
        }
        return course
    }

    fun createModule(currentUser: UserData, courseId: Int): ModuleData? {
        val newModuleData = getNewModuleDataFromUser()
        val module = courseService.createModule(currentUser, courseId, newModuleData)
        if (module != null) {
            println("Let's add lessons to your module...")
            do {
                createLesson(currentUser, courseId, module.id)
                val addAnotherLesson = getYesOrNo("Do you want to create another lesson(y/n) ?")
            } while (addAnotherLesson)
            return courseService.getModule(module.id)
        } else {
            println("Module creation failed")
            return null
        }
    }

    fun createLesson(currentUser: UserData, courseId: Int, moduleId: Int): LessonData ? {
        val newLessonData = getNewLessonDataFromUser()
        return courseService.createLesson(currentUser, courseId, moduleId, newLessonData)
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
                displayModule(
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
        if (getYesOrNo(
                "Are you ready to ${if (progress != null) "resume" else "start"} learning (y/n) ? "
            )
        ) {
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

    fun openCourse(currentUser: UserData, courseId: Int) {
        // Decide User Type
        var isAdmin = false
        var isStudent = false
        if (currentUser.role == UserRole.ADMIN) isAdmin = true
        else if (currentUser.role == UserRole.STUDENT) isStudent = true

        var course = courseService.getCourse(courseId)
        if (course == null) {
            return
        }

        // If Admin Then Show Edit Option
        if (isAdmin) {
            val editOptions = mutableMapOf(
                0 to "Go Back",
                1 to "Edit Basic Details",
                2 to "Edit Course Pricing",
                3 to "Add New Module"
            )
            if (course.modules.isNotEmpty())
                editOptions.put(4, "Edit Module Details")

            while (true) {
                if (course == null)
                    continue
                println()
                displayCourse(course, true)
                var isRefetch = false
                when (selectFromOption(editOptions)) {
                    0 -> break
                    1 -> {
                        if (editCoursePage.editCourseBasicDetails(currentUser, course))
                        // Refetch course After edit
                            isRefetch = true
                    }
                    2 -> {
                        editCoursePage.editCoursePricing(currentUser, courseId)
                        // Refetch course After edit
                        isRefetch = true
                    }
                    3 -> {
                        val moduleData = createModule(currentUser, courseId) ?: continue
                        println("New Module 👇")
                        displayModule(moduleData)
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
                            if (editCoursePage.editModuleDetails(currentUser, courseId, module.id))
                                isRefetch = true // Refetch Course
                        }
                    }
                }

                if (isRefetch)
                    course = courseService.getCourse(courseId)
            }
        }
        // If Student Then Show Enroll Option
        else if (isStudent) {
            displayCourse(course, true)
            // Get already enrolled courses ids
            val enrolledCourseIds = studentCourseService.getEnrolledCourseIds(currentUser.id)

            if (enrolledCourseIds.contains(courseId))
                println("Already Enrolled ✅")
            else {
                if (!getYesOrNo(
                        "Are you ready to learn new skills by enrolling this course (y/n) ? "
                    )) {
                    println("📚 No problem! You can start enroll anytime.")
                    return
                }

                val isEnrolled = enrollCourse(currentUser.id, course)
                println("Course enrollment is ${if (isEnrolled) "success ✅ " else "failed ❌"}")
                if (isEnrolled)
                    println("Course will be available in your 'My Course' page.")
            }
            if (getYesOrNo("Do you want to start learning (y/n) ? "))
                openCourseInLearningMode(course, currentUser)
        }
    }
}