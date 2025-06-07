package core.course.services

import core.course.repositories.AbstractCourseRepo
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
import db.inmemorystore.course.PriceDetails
import utils.getListInput
import java.util.UUID

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

fun String.capitalize(): String = this[0].uppercase() + this.substring(1).lowercase()

class CourseService (val courseRepo: AbstractCourseRepo) {

    private fun displayCourseCard(course: Course, priceDetails: PriceDetails?) {
        // TODO: Based on user role show different data in the card
        val cardWidth = 60
        val border = "═".repeat(cardWidth)
        val titleLine = "─".repeat(cardWidth)

        // Format duration
        val durationText = when {
            course.getDuration() > 60 ->
                "${(course.getDuration() / 60).toInt()}h ${(course.getDuration() % 60).toInt()}m"
            else -> "${course.getDuration()}m"
        }

        // Format price
        val priceText = if (course.isFreeCourse()) "Free" else {
            priceDetails?.let { "${it.getCurrencySymbol()}${it.getAmount()}" } ?: "Price not set"
        }

        // Format skills (limit to avoid overflow)
        val skills = course.getSkills()
        val skillsText = if (skills.size > 3) {
            skills.take(3).joinToString(", ") + " +${skills.size - 3} more"
        } else {
            skills.joinToString(", ")
        }

        fun centerText(text: String) = "${" ".repeat((cardWidth - text.length) / 2)}$text"

        println(buildString {
            appendLine("╔$border╗")
            appendLine(centerText(course.getTitle()))
            appendLine("╠${titleLine}╣")
            appendLine(" ID: ${course.getId()}")
            appendLine(" Level: ${course.getCourseLevel().toString().capitalize()}")
            appendLine(" Type: ${course.getCourseType().toString().capitalize()}")
            appendLine(" Duration: $durationText")
            appendLine(" Price: $priceText")
            appendLine(" Status: ${course.getStatus().toString().capitalize()}")
            appendLine(" Modules: ${course.getModuleIds().size} module(s)")
            if (skillsText.isNotEmpty()) {
                appendLine(" Skills: $skillsText")
            }
            val prerequisites = course.getPrerequisites()
            if (!prerequisites.isNullOrEmpty()) {
                val prereqText = if (prerequisites.size > 2) {
                    prerequisites.take(2).joinToString(", ") + " +${prerequisites.size - 2} more"
                } else {
                    prerequisites.joinToString(", ")
                }
                appendLine(" Prerequisites: $prereqText")
            }
            appendLine("╠$titleLine╣")
            appendLine(" ${course.getDescription()}")
            appendLine("╚$border╝")
        })
    }

    fun getCourses(searchQuery: String, offset: Int, limit: Int): List<Course> {
        return courseRepo.getCourse(searchQuery, offset, limit)
    }

    fun showCourses() {
        /* Steps:
          1. Show First 10.
              1.1. Open a specific course
              1.2. Load More(based on limit and retrieved data)
              1.3. Search
                   Go to step 1
         */
        var searchQuery = ""
        var offset = 0
        val limit = 1
        var isHaveMore = false

        fun displayCourses() {
            println("\n---------- Courses ----------")
            val courses = getCourses(searchQuery, offset, limit)
            if (courses.isEmpty()) {
                println("No Courses to display")
            }
            courses.forEach {
                val priceDetails: PriceDetails? = if(it.getPriceDetailsId() != null) {
                    courseRepo.getPriceDetails(it.getPriceDetailsId())
                } else {
                    null
                }
                displayCourseCard(it, priceDetails)
            }
            isHaveMore = courses.size == limit
        }
        displayCourses()

        while (true) {
            println("\nOption to choose ⬇️")
            println("0 -> Go Back")
            println("1 -> Open a course")
            println("2 -> Search by Course name 🔍")
            if (isHaveMore) println("3 -> Load More ↻")
            print("Enter your option: ")
            val userInput = readln().toInt()

            when (userInput) {
                // Go Back
                0 -> return
                // Select
                1 -> {

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
                    displayCourses()
                }
                // Load More
                3 -> {
                    if (!isHaveMore) {
                        println("No more courses to load")
                        continue
                    }
                    offset += limit
                    displayCourses()
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
        val categoryId = getSelectedCategoryIdFromUser().getId()

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
        fun displayCategories() {
            // handle empty
            println(String.format("%-5s | %-20s", "ID", "Category"))
            println("-".repeat(35))
            categories.forEach {
                println(String.format("%-5d | %-20s", it.getId(), it.getName()))
            }
            println("\nTotal ${categories.size} categories" +
                    if (searchQuery.isNotEmpty()) " for '$searchQuery'" else "")
        }
        displayCategories()

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

                    val selectedCategory = categories.find { it.getName().equals(input, true) }
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
                    displayCategories()
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
        println("$CURRENT_FILE_NAME: New Lesson(${lesson.getId()}) created successfully")
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

    fun createCourse(currentUserId: UUID): Course {
        // Create course with basic details
        val courseData = getBasicCourseDataFromUser()
        val course = courseRepo.createCourse(courseData, currentUserId)

        // Attach PriceDetails to Course
        val courseId = course.getId()
        if (courseData.priceData != null)
            courseRepo.createPriceDetails(courseId, courseData.priceData)
        println("${courseData.title}(id-${courseId}) created successfully with basic details")

        // Module & Lesson Creation
        do {
            val module = createModule(courseId, course.getModuleIds().size + 1)
            do {
                createLesson(courseId, module.getId(), module.getLessonIds().size)
                print("Do you want to create another lesson(y/n) ?")
                val addAnotherLesson = readln().lowercase() == "y"
            } while (addAnotherLesson)
            print("Do you want to create another module(y/n) ?")
            val addAnotherModule = readln().lowercase() == "y"
        } while (addAnotherModule)

        return course
    }
}