package core.course.services

import core.course.schemas.ModuleData
import db.ResourceStatus
import db.inmemorystore.course.Category
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.PriceDetails
import utils.formatDurationMinutes
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.take
import kotlin.text.isNotEmpty

class ConsoleDisplayService {
    /**
     * Displays a list of categories in a formatted table on the console.
     *
     * The table includes the ID and name of each category.
     * If a search query was used, it also displays that information at the end.
     *
     * @param categories A list of [Category] objects to display.
     */
    fun displayCategories(categories: List<Category>, searchQuery: String) {
        // handle empty
        println(String.format("%-5s | %-20s", "ID", "Category"))
        println("-".repeat(35))
        categories.forEach {
            println(String.format("%-5d | %-20s", it.getId(), it.getName()))
        }
        println("\nTotal ${categories.size} categories" +
                if (searchQuery.isNotEmpty()) " for '$searchQuery'" else "")
    }

    /**
     * Displays a Course with basic details optionally with price details and modules
     *
     * @param course course basic details to display
     * @param priceDetails price details os the course to display
     * @param moduleDataList List of ModuleData to display
     * @param isDetailedView Whether to include wrapping with counts or showing only partial content.
     */
    fun displayCourse(
        course: Course,
        priceDetails: PriceDetails? = null,
        moduleDataList: List<ModuleData>? = null,
        isDetailedView: Boolean = false,
    ) {
        // TODO: Based on user role show different data in the card
        val cardWidth = 60
        val border = "═".repeat(cardWidth)
        val titleLine = "─".repeat(cardWidth)

        // Format duration
        val durationText = when {
            course.getDuration() > 60 -> formatDurationMinutes(course.getDuration())
            else -> "${course.getDuration()}m"
        }

        // Format price
        val priceText = if (course.isFreeCourse()) "Free" else {
            priceDetails?.let { "${it.getCurrencySymbol()}${it.getAmount()}" } ?: "Price not set"
        }

        // Format skills (limit to avoid overflow)
        val skills = course.getSkills()
        val skillsText = if (!isDetailedView && skills.size > 3) {
            skills.take(3).joinToString(", ") + " +${skills.size - 3} more"
        } else {
            skills.joinToString(", ")
        }

        // Format Prerequisites (limit to avoid overflow)
        val prerequisites = course.getPrerequisites()
        val prereqText = prerequisites?.let {
            if (!isDetailedView && prerequisites.size > 2) {
                prerequisites.take(2).joinToString(", ") + " +${prerequisites.size - 2} more"
            } else {
                prerequisites.joinToString(", ")
            }
        }

        fun centerText(text: String) = "${" ".repeat((cardWidth - text.length) / 2)}$text"
        println("╔$border╗")
        println(centerText(course.getTitle()))
        println("╠${titleLine}╣")
        println(" ID: ${course.getId()}")
        println(" Description: ${course.getDescription()}")
        println(" Level: ${course.getCourseLevel().toString().capitalize()}")
        println(" Type: ${course.getCourseType().toString().capitalize()}")
        println(" Duration: $durationText")
        println(" Price: $priceText")
        println(" Status: ${course.getStatus().toString().capitalize()}")
        if (skillsText.isNotEmpty()) println(" Skills: $skillsText")
        if (prereqText != null && prereqText.isNotEmpty()) println(" Prerequisites: $prerequisites")
        if (moduleDataList != null) displayModules(moduleDataList, true)
        if (!isDetailedView) println("╚$border╝")
    }

    /**
     * Displays modules in hierarchical style with optional lessons
     *
     * @param modulesData List of ModuleData to display
     * @param withLessons Whether to include lessons under each module
     */
    fun displayModules(modulesData: List<ModuleData>, withLessons: Boolean = true) {
        if (modulesData.isEmpty()) {
            println(" No modules available.")
            return
        }

        println("=== MODULES ===\n")
        for((index, moduleData) in modulesData.withIndex()) {
            // Display module header
            println("${index + 1}. ${moduleData.module.getTitle()}")

            if (withLessons && !moduleData.lessons.isNullOrEmpty()) {
                // By default, lessons are sorted by sequence number
                moduleData.lessons.forEachIndexed { lessonIndex, lesson ->
                    val isLastLesson = lessonIndex == moduleData.lessons.size - 1
                    val prefix = if (isLastLesson) "   └── " else "   ├── "
                    println("$prefix ${lessonIndex + 1}. ${lesson.getTittle()} (${formatDurationMinutes(lesson.getDuration())})")
                }
            } else if (withLessons)
                println("   └── No lessons available")

            // Add spacing between modules
            if (index < modulesData.size - 1) println()
        }

        println("\n===============")
    }

    /**
     * Shows detailed information for a single lesson
     *
     * @param lesson The lesson to display
     * @param withResource Whether to include resource information
     */
    fun displayDetailedLesson(lesson: Lesson, withResource: Boolean = true) {
        val statusText = getStatusText(lesson.getStatus())

        println("=== LESSON DETAILS ===")
        println("Title: ${lesson.getTittle()}")
        println("ID: ${lesson.getId()}")
        println("Duration: ${formatDurationMinutes(lesson.getDuration())}")
        println("Sequence: ${lesson.getSequenceNumber()}")
        println("Status: $statusText")

        if (withResource) println("Resource: ${lesson.getResource()}")

        println("=====================")
    }

    private fun getStatusText(status: ResourceStatus): String {
        return when (status) {
            ResourceStatus.DRAFT -> "Draft"
            ResourceStatus.PUBLISHED -> "Published"
            ResourceStatus.ARCHIVE -> "Archive"
        }
    }
}