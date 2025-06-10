package core.course.services

import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.ResourceStatus
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
     * @param categories A list of [CategoryData] objects to display.
     */
    fun displayCategories(categories: List<CategoryData>, searchQuery: String) {
        // handle empty
        println(String.format("%-5s | %-20s", "ID", "Category"))
        println("-".repeat(35))
        categories.forEach {
            println(String.format("%-5d | %-20s", it.id, it.name))
        }
        println("\nTotal ${categories.size} categories" +
                if (searchQuery.isNotEmpty()) " for '$searchQuery'" else "")
    }

    /**
     * Displays a Course with basic details optionally with price details and modules
     *
     * @param course course details to display
     * @param isDetailedView Whether to include wrapping with counts or showing only partial content.
     */
    fun displayCourse(
        course: DetailedCourseData,
        isDetailedView: Boolean = false,
    ) {
        // TODO: Based on user role show different data in the card
        val cardWidth = 60
        val border = "═".repeat(cardWidth)
        val titleLine = "─".repeat(cardWidth)

        // Format duration
        val durationText = when {
            course.duration > 60 -> formatDurationMinutes(course.duration)
            else -> "${course.duration}m"
        }

        // Format price
        val priceText = if (course.isFreeCourse) {
            "Free"
        } else {
            course.priceDetails?.let { "${it.currencySymbol}${it.amount}" }
                ?: throw IllegalStateException(
                    "Price details missing in a non-free course(${course.id})"
                )
        }

        // Format skills (limit to avoid overflow)
        val skills = course.skills
        val skillsText = if (!isDetailedView && skills.size > 3) {
            skills.take(3).joinToString(", ") + " +${skills.size - 3} more"
        } else {
            skills.joinToString(", ")
        }

        // Format Prerequisites (limit to avoid overflow)
        val prereqText = course.prerequisites?.let {
            if (!isDetailedView && it.size > 2) {
                it.take(2).joinToString(", ") + " +${it.size - 2} more"
            } else {
                it.joinToString(", ")
            }
        }

        fun centerText(text: String) = "${" ".repeat((cardWidth - text.length) / 2)}$text"
        println("╔$border╗")
        println(centerText(course.title))
        println("╠${titleLine}╣")
        println(" ID: ${course.id}")
        println(" Description: ${course.description}")
        println(" Level: ${course.courseLevel.toString().capitalize()}")
        println(" Type: ${course.courseType.toString().capitalize()}")
        println(" Duration: $durationText")
        println(" Price: $priceText")
        println(" Status: ${course.status.toString().capitalize()}")
        if (skillsText.isNotEmpty()) println(" Skills: $skillsText")
        if (prereqText != null && prereqText.isNotEmpty()) println(" Prerequisites: ${course.prerequisites}")
        if (isDetailedView && course.modules.isNotEmpty())
            displayModules(course.modules, true, 2)
        if (!isDetailedView) println("╚$border╝")
    }

    /**
     * Displays modules in hierarchical style with optional lessons
     *
     * @param modulesData List of ModuleData to display
     * @param withLessons Whether to include lessons under each module
     */
    fun displayModules(modulesData: List<ModuleData>, withLessons: Boolean = true, prefixSpace: Int = 0) {
        if (modulesData.isEmpty()) {
            println(" No modules available.")
            return
        }

        println(" === MODULES ===\n")
        val space = " ".repeat(prefixSpace)
        for((index, moduleData) in modulesData.withIndex()) {
            // Display module header
            println("$space${index + 1}. ${moduleData.title}")

            if (withLessons && moduleData.lessons.isNotEmpty()) {
                // By default, lessons are sorted by sequence number
                moduleData.lessons.forEachIndexed { lessonIndex, lesson ->
                    val isLastLesson = lessonIndex == moduleData.lessons.size - 1
                    val prefix = space + if (isLastLesson) "   └── " else "   ├── "
                    println("$prefix ${lessonIndex + 1}. ${lesson.title} (${formatDurationMinutes(lesson.duration)})")
                }
            } else if (withLessons)
                println("$space   └── No lessons available")

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
    fun displayDetailedLesson(lesson: LessonData, withResource: Boolean = true) {
        val statusText = getStatusText(lesson.status)

        println("=== LESSON DETAILS ===")
        println("Title: ${lesson.title}")
        println("ID: ${lesson.id}")
        println("Duration: ${formatDurationMinutes(lesson.duration)}")
        println("Sequence: ${lesson.sequenceNumber}")
        println("Status: $statusText")

        if (withResource) println("Resource: ${lesson.resource}")

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