package core.course.services

import core.course.repositories.AbstractCourseRepo
import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import db.CourseLevel
import db.CourseType
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.Module
import db.inmemorystore.user.User
import utils.getListInput

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

fun String.capitalize(): String = this[0].uppercase() + this.substring(1).lowercase()

class CourseService (val courseRepo: AbstractCourseRepo) {
    private val displayService = ConsoleDisplayService()

    /**
     * Retrieves detailed information for a specific course by its ID.
     *
     * @param courseId The unique identifier of the course.
     * @return A [DetailedCourseData] object containing full course information includes pricing, modules, etc.
     */
    fun getCourse(courseId: Int): DetailedCourseData? {
        // Get Course Basic Details
        val course: Course? = courseRepo.getCourse(courseId)
        if (course == null) {
            println("No course found at the for course id($courseId)")
            return null
        }
        val detailedCourseData = DetailedCourseData(course)

        // Get Price Details
        if (course.getPriceDetailsId() != null)
            detailedCourseData.priceDetails = courseRepo.getPriceDetails(course.getPriceDetailsId())

        // Get Modules
        val moduleIds = course.getModuleIds()
        if (moduleIds.isEmpty()) return detailedCourseData
        val modules: List<Module> = courseRepo.getModules(moduleIds)

        // Get Lessons
        detailedCourseData.modules = modules.filter {
            it.getLessonIds().isNotEmpty()
        }.map {
            val lessonIds = it.getLessonIds()
            val lessons: List<Lesson> = courseRepo.getLessons(lessonIds)
            ModuleData (it, lessons)
        }

        return detailedCourseData
    }

    /**
     * Retrieves a paginated list of courses that match the given search query.
     *
     * @param searchQuery to search courses by title.
     * @param offset The starting index for pagination.
     * @param limit The maximum number of courses to return.
     * @return A list of [Course] objects that match the search criteria.
     */
    fun getCoursesWithPriceDetails(searchQuery: String, offset: Int, limit: Int): List<DetailedCourseData> {
        val courses = courseRepo.getCourses(searchQuery, offset, limit)
        return courses.map {
            val priceDetails = if(it.getPriceDetailsId() != null) {
                courseRepo.getPriceDetails(it.getPriceDetailsId())
            } else {
                null
            }
            DetailedCourseData(it, priceDetails)
        }
    }

    /**
     * Shows list of courses. Each courses with its minimal details.
     */
    fun listCourses(currentUser: User) {
        var searchQuery = ""
        var offset = 0
        val limit = 10
        var hasMore = false

        fun fetchCourses() {
            val courses = getCoursesWithPriceDetails(searchQuery, offset, limit)
            if (courses.isEmpty()) {
                println("-------------- No Course to display -------------")
                hasMore = false
                return
            }
             courses.forEach {
                 displayService.displayCourse(it.course, it.priceDetails)
             }
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
                    val detailedCourseData = getCourse(courseId)
                    if (detailedCourseData == null)
                        continue
                    displayService.displayCourse(
                        detailedCourseData.course,
                        detailedCourseData.priceDetails,
                        detailedCourseData.modules
                    )
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
        println("Enter Course Level(${CourseLevel.entries.joinToString(", ")}):")
        val courseLevel = readln().trim().let { CourseLevel.getFromStrValue(it) } // Reason for using let: Better readability and clarity
        println("Enter Course Type(${CourseType.entries.joinToString(", ")}):")
        val courseType = readln().trim().let { CourseType.getFromStrValue(it) }

        // Free course check with Price details
        print("Is this a free course? (y/n): ")
        val isFreeInput = readln().trim().lowercase()
        val isFree = isFreeInput == "y"
        var priceData: NewPriceData? = null
        if (!isFree) {
            println("\n----- Enter Price Details -----")
            val currencyMap = mapOf<String, String>(
                "INR" to "₹",
                "USD" to "$",
                // Add other entries
            )
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

        // Get Category ID
        val categoryId = getSelectedCategoryIdFromUser().id

        return NewCourseBasicData(
            title=title,
            description=description,
            skills=skills,
            courseLevel=courseLevel,
            courseType=courseType,
            isFreeCourse=isFree,
            categoryId=categoryId,
            prerequisites=prerequisites,
            priceData = priceData,
        )
    }

    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        return courseRepo.getCategories(searchQuery, offset, limit)
    }

    private fun getSelectedCategoryIdFromUser(): Category {
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
        var categories: List<Category> = getCategories(searchQuery, offset, limit)

        // Show 10 default categories
        displayService.displayCategories(categories, searchQuery)

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
                    displayService.displayCategories(categories, searchQuery)
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

    private fun getNewModuleDataFromUser(): NewModuleData {
        println("----- Module Creation ------")
        print("Enter module title: ")
        val title = readln().trim()

        print("Enter description (optional, press enter to skip): ")
        val description = readln().trim().ifBlank { null }

        return NewModuleData(title = title, description = description)
    }

    private fun getNewLessonDataFromUser(sequenceNumber: Int): NewLessonData {
        println("----- Lesson Creation ------")
        print("Enter Lesson title: ")
        val title = readln().trim()
        print("Enter content: ")
        val resource = readln().trim()
        print("Enter duration(in minutes): ")
        val duration = readln().toFloat()

        return NewLessonData(
            title = title,
            resource = resource,
            duration = duration,
            sequenceNumber = sequenceNumber,
        )
    }

    fun createLesson(courseId: Int, moduleId: Int, sequenceNumber: Int): Lesson {
        val newLessonData =  getNewLessonDataFromUser(sequenceNumber)
        newLessonData.sequenceNumber = sequenceNumber
        val lesson = courseRepo.createLesson(newLessonData, moduleId)
        println("$CURRENT_FILE_NAME: New Lesson(${lesson.id}) created successfully")
        // Increase duration in Module
        val updatedModuleDuration: Float = courseRepo.updateModuleDuration(moduleId, lesson.getDuration())
        println("$CURRENT_FILE_NAME: Module($moduleId) duration updated")
        // Increase duration in Course
        courseRepo.updateCourseDuration(courseId, updatedModuleDuration)
        println("$CURRENT_FILE_NAME: Course($courseId) duration updated")
        return lesson
    }

    fun createModule(courseId: Int, sequenceNumber: Int): Module {
        val newModule =  getNewModuleDataFromUser()
        newModule.sequenceNumber = sequenceNumber
        val module = courseRepo.createModule(newModule, courseId)
        return module
    }

    fun createCourse(currentUser: User): Course {
        // TODO: Role Check

        // Create course with basic details
        val courseData = getBasicCourseDataFromUser()
        val course = courseRepo.createCourse(courseData, currentUser.id)

        // Attach PriceDetails to Course
        val courseId = course.id
        if (courseData.priceData != null)
            courseRepo.createPriceDetails(courseId, courseData.priceData)
        println("${courseData.title}(id-${courseId}) created successfully with basic details")

        // Module & Lesson Creation
        do {
            val module = createModule(courseId, course.getModuleIds().size + 1)
            do {
                createLesson(courseId, module.id, module.getLessonIds().size)
                print("Do you want to create another lesson(y/n) ?")
                val addAnotherLesson = readln().lowercase() == "y"
            } while (addAnotherLesson)
            print("Do you want to create another module(y/n) ?")
            val addAnotherModule = readln().lowercase() == "y"
        } while (addAnotherModule)

        return course
    }
}