package core.course.services

import core.course.repositories.AbstractCourseRepo
import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.schemas.CourseLevel
import core.course.schemas.CourseType
import core.course.schemas.PriceDetailsData
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import core.user.schemas.UserRole
import core.user.schemas.UserData
import utils.getListInput
import kotlin.Int

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

fun String.capitalize(): String = this[0].uppercase() + this.substring(1).lowercase()

val currencyMap = mapOf<String, String>(
    "INR" to "₹",
    "USD" to "$",
    // Add other entries
)

class CourseService (
    private val courseRepo: AbstractCourseRepo,
    private val studentCourseService: StudentCourseService
) {
    /**
     * Retrieves detailed information for a specific course by its ID.
     *
     * @param courseId The unique identifier of the course.
     * @return A [DetailedCourseData] object containing full course information includes pricing, modules, etc.
     */
    fun getCourse(courseId: Int): DetailedCourseData? = courseRepo.getCourse(courseId)

    /**
     * Retrieves a paginated list of courses that match the given search query.
     *
     * @param searchQuery to search courses by title.
     * @param offset The starting index for pagination.
     * @param limit The maximum number of courses to return.
     * @return A list of [DetailedCourseData] objects.
     */
    fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        currentUser: UserData,
        onlyAssociated: Boolean = false,
    ): List<DetailedCourseData> {
        // TODO: Get Courses based on roles

        var courseIds: List<Int>? = null
        if (currentUser.role != UserRole.ADMIN && onlyAssociated) {
            if (currentUser.role == UserRole.STUDENT)
                // Get Student's Enrolled Course Ids
                courseIds = studentCourseService.getEnrolledCourseIds(currentUser.id)
//          else {
//                // Get Trainer's Assigned Course Ids
//          }

            // If no course found then return immediately
            if (courseIds != null && courseIds.isEmpty()) {
                println("No course found for user(${currentUser.fullName})")
                return emptyList()
            }
        }

        return courseRepo.getCourses(searchQuery, offset, limit, courseIds)
    }

    fun getCoursesByIds(courseIds: List<Int>): List<DetailedCourseData> {
        return courseRepo.getCoursesByIds(courseIds)
    }

    fun getLesson(lessonId: Int) = courseRepo.getLesson(lessonId)

    fun getModule(moduleId: Int) = courseRepo.getModule(moduleId)

    fun getCoursePriceDetails(courseId: Int) = courseRepo.getPriceDetails(courseId)

    /**
     * Fetches a paginated list of course categories, with an optional search filter.
     *
     * @param searchQuery The text to filter categories by name.
     * @param offset The starting index for pagination (e.g., 0 for the first page).
     * @param limit The maximum number of categories in one call.
     * @return A list of [CategoryData] matching the criteria.
     */
    fun getCategories(searchQuery: String, offset: Int, limit: Int) =
        courseRepo.getCategories(searchQuery, offset, limit)

    /**
     * Interactively prompts the user to enter all the basic details for creating a new course.
     *
     * @return A [NewCourseBasicData] object populated with the user's input.
     */
    private fun getBasicCourseDataFromUser(): NewCourseBasicData {
        println("---------- Course Creation Section ----------")
        print("Enter course title: ")
        val title = readln().trim()
        print("Enter course description: ")
        val description = readln().trim()

        // Skills & Prerequisites
        val skills = getListInput("Enter skills(separate by comma): ", ",")
        val prerequisites = getListInput(
            "Enter prerequisites (separate by comma, or press enter to skip): ",
            ","
        )

        // Course Level & Type
        println("Enter Course Level(${CourseLevel.entries.joinToString(", ") { it.name.capitalize() }}):")
        val courseLevel = readln().trim().let { CourseLevel.getFromStrValue(it) } // Reason for using let: Better readability and clarity
        println("Enter Course Type(${CourseType.entries.joinToString(", ") { it.name.capitalize().replace("_", "-") }}):")
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
                println("Invalid amount entered. Setting base price as 0")
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

    /**
     * Provides an interactive command-line interface for the user to select a course category.
     *
     * @return The final [CategoryData] object selected by the user.
     */
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
        var categories: List<CategoryData> = getCategories(searchQuery, offset, limit)

        // Show 10 default categories
        CourseDisplayService.displayCategories(categories, searchQuery)

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
                    categories = getCategories(searchQuery, offset, limit)
                    CourseDisplayService.displayCategories(categories, searchQuery)
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

    /**
     * Prompts the user to enter the module details.
     *
     * @return A [NewModuleData] object containing the entered details.
     */
    private fun getNewModuleDataFromUser(): NewModuleData {
        println("----- Module Creation ------")
        print("Enter module title: ")
        val title = readln().trim()

        print("Enter description (optional, press enter to skip): ")
        val description = readln().trim().ifBlank { null }

        return NewModuleData(title = title, description = description)
    }

    /**
     * Prompts the user to enter the details for a new lesson.
     *
     * @param sequenceNumber The sequential order of this lesson within its parent module.
     * @return A [NewLessonData] object populated with the user's input and sequence number.
     */
    private fun getNewLessonDataFromUser(sequenceNumber: Int): NewLessonData {
        println("----- Lesson Creation ------")
        print("Enter Lesson title: ")
        val title = readln().trim()
        print("Enter content: ")
        val resource = readln().trim()
        print("Enter duration(in minutes): ")
        val duration = readln().toInt()

        return NewLessonData(
            title = title,
            resource = resource,
            duration = duration,
            sequenceNumber = sequenceNumber,
        )
    }

    /**
     * Creates a new lesson within a specific module and updates the duration of the parent module and course.
     *
     * @param courseId The ID of the parent course, used to update its total duration.
     * @param moduleId The ID of the module to which this lesson will be added.
     * @param sequenceNumber The sequential order of this lesson within the module.
     * @return A [LessonData] object representing the newly created lesson.
     */
    fun createLesson(courseId: Int, moduleId: Int, sequenceNumber: Int):  LessonData {
        val newLessonData =  getNewLessonDataFromUser(sequenceNumber)
        newLessonData.sequenceNumber = sequenceNumber
        val lesson = courseRepo.createLesson(newLessonData, moduleId)
        return lesson
    }

    /**
     * Creates a new module for a given course.
     *
     * @param courseId The ID of the course to which this module belongs.
     * @param sequenceNumber The sequential order of this module within the course.
     * @return A [ModuleData] object for the newly created module.
     */
    fun createModule(courseId: Int, sequenceNumber: Int): ModuleData {
        val newModule =  getNewModuleDataFromUser()
        newModule.sequenceNumber = sequenceNumber
        val module = courseRepo.createModule(newModule, courseId)
        return module
    }

    /**
     * Orchestrates the end-to-end process of creating a new course.
     *
     * This function handles the entire course creation workflow:
     * 1. Checks if the `currentUser` has ADMIN permissions.
     * 2. Gathers basic course details from the user.
     * 3. Interactively prompts the user to create one or more modules with one or more lessons.
     *
     * @param currentUser The user attempting to create the course.
     * @return A [DetailedCourseData] the complete course object with all its modules
     *         and lessons, or `null` if the user does not have the required permissions.
     */
    fun createCourse(currentUser: UserData): DetailedCourseData? {
        if (currentUser.role != UserRole.ADMIN) {
            println("User don't have the permission to create course")
            return null
        }

        // Create course with basic details
        val newCourse = getBasicCourseDataFromUser()
        val course = courseRepo.createCourse(newCourse, currentUser.id)

        // Attach PriceDetails to Course
        val courseId = course.id
        if (newCourse.priceData != null)
            courseRepo.createPricing(newCourse.priceData, courseId)
        println("${newCourse.title}(id-${courseId}) created successfully with basic details")

        // Module & Lesson Creation
        do {
            val module = createModule(courseId, course.modules.size + 1)
            do {
                createLesson(courseId, module.id, module.lessons.size + 1)
                print("Do you want to create another lesson(y/n) ?")
                val addAnotherLesson = readln().lowercase() == "y"
            } while (addAnotherLesson)
            print("Do you want to create another module(y/n) ?")
            val addAnotherModule = readln().lowercase() == "y"
        } while (addAnotherModule)

        println("Course successfully created!!!")
        return course
    }

    fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData) {
        courseRepo.updateCourseBasicDetails(courseId, updateData)
        println("Course($courseId) basic details updated.")
    }

    fun updateCoursePricing(courseId: Int, priceDetails: UpdatePriceDetailsData?) {
        courseRepo.updateOrCreatePricing(priceDetails, courseId)
        println("Price Details Updated.")
    }

    fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData) {
        courseRepo.updateModuleDetails(moduleId, updateData)
        println("Module($moduleId) details updated.")
    }

    fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData) {
        courseRepo.updateLessonDetails(lessonId, updateData)
        println("Lesson($lessonId) details updated.")
    }

    fun deleteLesson(lessonId: Int): Boolean {
        return courseRepo.deleteLesson(lessonId)
    }
}