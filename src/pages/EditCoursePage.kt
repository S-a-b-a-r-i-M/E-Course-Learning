package pages

import core.course.schemas.DetailedCourseData
import core.course.schemas.ModuleData
import core.course.schemas.PriceDetailsData
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import core.course.services.CourseService
import core.user.schemas.UserData
import utils.Result
import utils.currencyMap
import utils.displayDetailedLesson
import utils.displayModule
import utils.getListInput
import utils.getNewLessonDataFromUser
import utils.getYesOrNo
import utils.selectFromOption
import utils.selectResourceStatus

class EditCoursePage (val courseService: CourseService) {
    fun editPriceDetails(currentUser: UserData, courseId: Int): Boolean {
        val priceDetailsData = courseService.getCoursePriceDetails(courseId) ?: return false
        val updatedPriceData = UpdatePriceDetailsData(id=priceDetailsData.id)
        var isModified = false

        val options = mapOf(
            1 to "Currency Code",
            2 to "Amount",
            3 to "Discard & Go Back",
            4 to "Save & Go Back"
        )
        while (true) {
            println("\n===== Edit Price Details =====")
            when(selectFromOption(options)) {
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
                        if (getYesOrNo("Warning: Changes wont get saved. Are you sure (y/n) ?"))
                            return false
                    } else
                        return false
                }
                4 -> {
                    return when(val result = courseService.updateCoursePricing(currentUser, courseId, updatedPriceData)){
                        is Result.Error -> {
                            println("⚠️: ${result.message}")
                            false
                        }
                        is Result.Success -> {
                            println("Price details updated successfully")
                            true
                        }
                    }
                }
            }
        }
    }

    fun editCoursePricing(currentUser: UserData, courseId: Int) {
        fun getPriceDetails(): UpdatePriceDetailsData {
            print("Enter currency code (${currencyMap.keys.joinToString(", ")}): ")
            val currencyCode = readln().trim().uppercase()
            val currencySymbol = currencyMap.getOrDefault(currencyCode, "₹")
            while (true) {
                print("Enter amount: ")
                val amount = readln().toDoubleOrNull()
                if (amount == null || amount <= 0){
                    println("Invalid amount entered. Try again")
                    continue
                }
                return UpdatePriceDetailsData(0, currencyCode, currencySymbol, amount)
            }
        }

        var priceDetails: PriceDetailsData? = null
        fun fetchPriceDetails() {
            priceDetails = courseService.getCoursePriceDetails(courseId)
        }
        fetchPriceDetails() // Initial fetch

        val options = mutableMapOf(
            0 to "Go Back",
            1 to "Free/Paid Status"
        )
        val isFreeCourse = priceDetails != null
        if (isFreeCourse) options[2] = "Price Details"

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

    private fun getTitleFromUser(): String {
        print("Enter New title or press Enter to keep current (min 3 char, max 50 char): ")
        val input = readln().trim()

        // Validate
        if (input.isEmpty())
            return ""
        else if (input.length < 3) {
            println("Title is too short...")
            return ""
        } else if (input.length > 50) {
            println("Title is too big...")
            return ""
        }

        return input
    }

    fun editLesson(currentUser: UserData, lessonId: Int, courseId: Int, moduleId: Int): Boolean {
        val existingLessonData = courseService.getLesson(lessonId) ?: return false
        val updateLessonData = UpdateLessonData()
        var isModified = false
        val options = mapOf(
            1 to "Edit Title",
            2 to "Edit Resource",
            3 to "Edit Duration",
            4 to "Edit Status",
            5 to "Discard & Go Back",
            6 to "Save & Go Back"
        )

        println("\nSelected Lesson to Edit 👇")
        displayDetailedLesson(existingLessonData, true)
        while (true) {
            println("\n===== Edit Lesson =====")

            when (selectFromOption(options)) {
                1 -> {
                    println("Current: ${updateLessonData.title ?: existingLessonData.title}")
                    try {
                        val newTitle = getTitleFromUser()
                        if (newTitle.isNotEmpty()) {
                            updateLessonData.title = newTitle
                            isModified = true
                            println("New Title added ✅")
                        }
                        else
                            println("Title unchanged")
                    } catch (exp: Exception) {
                        println("Err:{${exp.message}}")
                        println("Try again....\n")
                    }

                }
                2 -> {
                    println("Current: ${updateLessonData.resource ?: existingLessonData.resource}")
                    print("New resource or press Enter to keep current (min 30 char): ")
                    val newResource = readln().trim()
                    if (newResource.isEmpty())
                        println("Resource unchanged")
                    if (newResource.length < 30)
                        println("Resource cannot be less than 30 characters...")
                    else  {
                        updateLessonData.resource = newResource
                        isModified = true
                        println("New Resource added")
                    }
                }
                3 -> {
                    println("Current: ${updateLessonData.newDuration ?: existingLessonData.duration} minutes")
                    print("New duration or press Enter to keep current(ex: 30, 45): ")
                    val newDuration = readln().toIntOrNull()
                    if (newDuration != null && newDuration > 0) {
                        updateLessonData.newDuration = newDuration
                        updateLessonData.oldDuration = existingLessonData.duration
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
                // Discard & Go Back
                5 -> {
                    if (isModified) {
                        if (getYesOrNo("Warning: Changes wont get saved. Are you sure (y/n) ?"))
                            return false
                    } else
                        return false
                }
                // Save & Go Back
                6 -> {
                    return when (val result = courseService.updateLessonDetails(
                        currentUser,
                        courseId,
                        moduleId,
                        existingLessonData.id,
                        updateLessonData
                    )) {
                        is Result.Error -> {
                            println("⚠️: ${result.message}")
                            false
                        }
                        is Result.Success -> {
                            println("Lesson details updated successfully ✅")
                            true
                        }
                    }
                }
            }
        }
    }

    fun manageLessons(currentUser: UserData, courseId: Int, moduleId: Int) {
        fun fetchModule() = courseService.getModule(moduleId)
        var module: ModuleData = fetchModule() ?: return

        val options = mutableMapOf(0 to "Go Back", 1 to "Add Lesson")
        if (module.lessons.isNotEmpty()) options.put(2, "Edit Lesson")

        while (true) {
            // display lessons
            println("\n===== Manage Lessons =====")
            when (selectFromOption(options)) {
                // Go Back
                0 -> break
                // Add Lesson
                1 -> {
                    val newLessonData = getNewLessonDataFromUser()
                    val lesson = courseService.createLesson(
                        currentUser, courseId, moduleId, newLessonData
                    ) ?: continue
                    println("New Lesson 👇")
                    displayDetailedLesson(lesson, true)
                    module = fetchModule() ?: return // Refetch
                }
                // Edit Lesson
                2 -> {
                    if (module.lessons.isEmpty()) {
                        println("Invalid option selected. Please try again.")
                        continue
                    }

                    print("Enter lesson number to edit:")
                    val inputIdx = readln().toInt() - 1
                    if (inputIdx < 0 || inputIdx > module.lessons.size) {
                        println("Invalid input, try again.")
                        continue
                    }
                    // Find the lesson
                    val lessonData = module.lessons[inputIdx]
                    if (editLesson(currentUser, lessonData.id, courseId, moduleId))
                        module = fetchModule() ?: return
                }
                else -> println("Invalid option selected. Please try again.")
            }
        }
    }

    fun editModuleDetails(currentUser: UserData, courseId: Int, moduleId: Int): Boolean {
        val module = courseService.getModule(moduleId) ?: return false
        val updateModuleData = UpdateModuleData()
        var isModified = false

        val options = mapOf(
            1 to "Edit Title",
            2 to "Edit Description",
            3 to "Edit Status",
            4 to "Manage Lessons",
            5 to "Discard & Go Back",
            6 to "Save & Go Back"
        )
        println("\nSelected Module to Edit 👇")
        displayModule(module) // Display selected module
        while (true) {
            println("\n===== Edit Module =====")

            when (selectFromOption(options)) {
                1 -> {
                    println("Current: ${updateModuleData.title ?: module.title}")
                    val newTitle = getTitleFromUser()
                    if (newTitle.isNotEmpty()) {
                        updateModuleData.title = newTitle
                        isModified = true
                        println("New title added")
                    }
                    else
                        println("Title unchanged")
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
                        if (getYesOrNo("Warning: Changes wont get saved. Are you sure (y/n) ?"))
                            return false
                    } else
                        return false
                }
                // Save & Go Back
                6 -> {
                    return when(val result = courseService.updateModuleDetails(
                            currentUser, module.id, updateModuleData
                        )) {
                        is Result.Error -> {
                            println("⚠️: ${result.message}")
                            false
                        }
                        is Result.Success -> {
                            println("Module($moduleId) details updated.")
                            true
                        }
                    }
                }
            }
        }
    }

    fun editCourseBasicDetails(currentUser: UserData, courseData: DetailedCourseData): Boolean {
        val updateCourseData = UpdateCourseBasicData()
        var isModified = false
        val options = mapOf(
            1 to "Title",
            2 to "Description",
            3 to "Skills",
            4 to "Prerequisites",
            5 to "Status",
            6 to "Discard & Go Back",
            7 to "Save & Go Back"
        )

        while (true) {
            println("\n===== Edit Basic Details =====")
            when (selectFromOption(options)) {
                // Title
                1 -> {
                    println("Current: ${updateCourseData.title ?: courseData.title}")
                    val newTitle = getTitleFromUser()
                    if (newTitle.isNotEmpty() && newTitle != courseData.title) {
                        updateCourseData.title = newTitle
                        isModified = true
                        println("New title added ✅")
                    }
                    else
                        println("Title unchanged")
                }
                // Description
                2 -> {
                    println("Current: ${updateCourseData.description ?: courseData.description}")
                    print("Enter new Description or press Enter to keep current(min 10 char): ")
                    val newDescription = readln().trim()
                    if (newDescription.isEmpty()) println("Description unchanged.")
                    else if (newDescription.length < 10) println("Description is too small...")
                    else if (newDescription != courseData.description) {
                        updateCourseData.description = newDescription
                        isModified = true
                        println("New description added.")
                    }
                }
                // Skills
                3 -> {
                    println("Current: ${(updateCourseData.skills ?: courseData.skills).joinToString(", ")}")
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
                    println("Current: ${
                        (updateCourseData.prerequisites ?: courseData.prerequisites)?.joinToString(", ") 
                            ?: "None"}"
                    )
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
                        if (getYesOrNo("Warning: Changes wont get saved. Are you sure (y/n) ?"))
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
}