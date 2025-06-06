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
import utils.getListInput
import java.util.UUID

class CourseService (val courseRepo: AbstractCourseRepo) {
    fun getCourse() {
        courseRepo.getCourse()
    }

    private fun getBasicCourseDataFromUser(): NewCourseBasicData {
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
            println("----- Enter Price Details -----")
            val currencyMap = mapOf<String, String>(
                "INR" to "₹",
                "USD" to "$",
                // Add other entries
            )
            print("Enter currency code (${currencyMap.keys.joinToString(", ")}): ")
            val currencyCode = readln().trim().uppercase()
            val currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")

            print("Enter amount:")
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

    fun getSelectedCategoryIdFromUser(): Category {
        /* Steps:
          1. Show First 10.
              1.1. Select Categories
              1.2. Load More(based on limit and retrieved data)
              1.3. Search
                   Go to step 1
                   1.3.1. If no categories found then create and ask user to search again to get the id
         */
        println("----- Choose Course Category -----")
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
                println(String.format("%-5d | %-20s", it.id, it.name))
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
                    print("Enter a category:")
                    val input = readln()
                    if (input.isEmpty()) {
                        println("Input is not a number. Please try again.")
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

    fun getNewModuleDataFromUser(): NewModuleData {
        println("----- Module Creation ------")
        print("Enter module title: ")
        val title = readln().trim()

        print("Enter description (optional, press enter to skip): ")
        val description = readln().trim().ifBlank { null }

        return NewModuleData(title = title, description = description)
    }

    fun createModule(courseId: Int, sequenceNumber: Int): Module {
        val newModule =  getNewModuleDataFromUser()
        newModule.sequenceNumber = sequenceNumber
        val module = courseRepo.createModule(newModule, courseId)
        return module
    }

    fun getNewLessonDataFromUser(sequenceNumber: Int): NewLessonData {
        println("----- Lesson Creation ------")
        print("Enter Lesson title: ")
        val title = readln().trim()
        print("Enter content: ")
        val resource = readln().trim()
        print("Enter duration: ")
        val duration = readln().toFloat()

        return NewLessonData(
            title = title,
            resource = resource,
            duration = duration,
            sequenceNumber = sequenceNumber,
        )
    }

    fun createLesson(moduleId: Int, sequenceNumber: Int): Lesson {
        val newLessonData =  getNewLessonDataFromUser(sequenceNumber)
        newLessonData.sequenceNumber = sequenceNumber
        val lesson = courseRepo.createLesson(newLessonData, moduleId)
        return lesson
    }

    fun createCourse(currentUserId: UUID): Course {
        val courseData = getBasicCourseDataFromUser()
        val course = courseRepo.createCourse(courseData, currentUserId)

        // Attach PriceDetails To Course
        if (courseData.priceData != null)
            courseRepo.createPriceDetails(course.id, courseData.priceData)
        println("${courseData.title}(id-${course.id}) created successfully with basic details")

        // Module Creation
        var wantsAnotherModule = true
        while (wantsAnotherModule) {
            val module = createModule(course.id, course.moduleIds.size + 1)
            var wantsAnotherLesson = true
            while (wantsAnotherLesson) {
                createLesson(module.id, module.lessonIds.size)
                println("Do you want to create another lesson(y/n) ?")
                wantsAnotherLesson = readln().lowercase() == "y"
            }
            println("Do you want to create another module(y/n) ?")
            wantsAnotherModule = readln().lowercase() == "y"
        }
        val moduleId = createModule(course.id, 0)

        return course
    }

    fun getCategories(searchQuery: String, offset: Int, limit: Int): List<Category> {
        return courseRepo.getCategories(searchQuery, offset, limit)
    }
}