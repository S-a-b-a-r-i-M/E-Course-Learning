package utils

import core.course.schemas.CourseLevel
import core.course.schemas.CourseType
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.services.capitalize

/**
 * Prompts the user to enter the module details.
 *
 * @return A [NewModuleData] object containing the entered details.
 */
fun getNewModuleDataFromUser(): NewModuleData {
    println("----- Module Creation ------")
    while (true) {
        print("Enter module title (min 3 char, max 50 char): ")
        val title = InputValidator.validateName(readln(), "Title", 3, 50)

        print("Enter description (optional, press enter to skip): ")
        val description = readln().trim().ifBlank { null }

        return NewModuleData(title = title, description = description)
    }
}

/**
 * Prompts the user to enter the details for a new lesson.
 *
 * @return A [NewLessonData] object populated with the user's input and sequence number.
 */
fun getNewLessonDataFromUser(): NewLessonData {
    while (true) {
        try {
            println("----- Lesson Creation ------")
            print("Enter Lesson title (min 3 char, max 50 char): ")
            val title = InputValidator.validateName(readln(), "Title", 3, 50)
            print("Enter content (min 30 char): ")
            val resource = InputValidator.validateName(readln(), "Content", 30)
            print("Enter duration in minutes (ex: 30, 45): ")
            val duration = InputValidator.validatePositiveInt()

            return NewLessonData(
                title = title,
                resource = resource,
                duration = duration,
            )
        } catch (exp: Exception) {
            println("Err:{${exp.message}}")
            println("Try again....\n")
        }
    }
}

fun getNewCourseBasicDataFromUser(): NewCourseBasicData {
    while (true) {
        try {
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
            val courseLevel = readln().trim()
                .let { CourseLevel.getFromStrValue(it) } // Reason for using let: Better readability and clarity
            print(
                "Enter Course Type(${
                    CourseType.entries.joinToString(", ") {
                        it.name.capitalize().replace("_", "-")
                    }
                }):"
            )
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

                print("Enter amount(should be positive): ")
                val amount = InputValidator.validatePositiveDouble()
                priceData = NewPriceData(currencyCode, currencySymbol, amount)
            }

            return NewCourseBasicData(
                title = title,
                description = description,
                skills = skills,
                courseLevel = courseLevel,
                courseType = courseType,
                category = "",
                prerequisites = prerequisites,
                priceData = priceData,
            )
        } catch (exp: Exception) {
            println("Err:{${exp.message}}")
            println("Try again....\n")
        }
    }
}