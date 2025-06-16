package core.course.services

import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.ResourceStatus
import db.CompletionStatus
import utils.formatDurationMinutes
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.take
import kotlin.text.isNotEmpty

object CourseDisplayService {
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
    fun displayCourse(course: DetailedCourseData, isDetailedView: Boolean = false) {
        // TODO: Based on user role show different data in the card
        val cardWidth = 60
        val border = "═".repeat(cardWidth)
        val titleLine = "─".repeat(cardWidth)

        // Format duration
//        val durationText = when {
//            course.duration > 60 -> formatDurationMinutes(course.duration)
//            else -> "${course.duration}m"
//        }
        val durationText = ""

        // Format price
        val priceText = course.priceDetails?.let { "${it.currencySymbol}${it.amount}" } ?: "Free"

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
        if (prereqText != null && prereqText.isNotEmpty()) println(" Prerequisites: $prereqText")
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
    fun displayModules(
        modulesData: List<ModuleData>,
        withLessons: Boolean = true,
        prefixSpace: Int = 0,
    ) {
        if (modulesData.isEmpty()) {
            println(" No modules available.")
            return
        }

        println(" === MODULES ===")
        for((index, moduleData) in modulesData.withIndex()) {
            displayModule(moduleData, withLessons, prefixSpace, index + 1)
            // Add spacing between modules
            if (index < modulesData.size - 1) println()
        }
        println(" ===============")
    }

    fun displayModule(
        moduleData: ModuleData,
        withLessons: Boolean = true,
        prefixSpace: Int = 0,
        indexNumber: Int? = null,
        isReadMode: Boolean = false,
        recentLessonId: Int = -1,
        recentLessonStatus: CompletionStatus = CompletionStatus.NOT_STARTED,
    ) {
        val space = " ".repeat(prefixSpace)

        // Display module header
        println("$space${if (indexNumber != null) "$indexNumber." else ""}ID: ${moduleData.id}")
        println("${space}Title: ${moduleData.title}")
        if (moduleData.description != null) {
            println("${space}Description: ${moduleData.description}")
        }
        if (!isReadMode) println("${space}Status: ${moduleData.status.name.capitalize()}")
//       else println("${space}Status: ${moduleData.status.name.capitalize()}") // Module Level Status and Completion percentage
        println("${space}Duration: ${formatDurationMinutes(moduleData.duration)}")
        println("${space}Lessons(${moduleData.lessons.size}): ")

        if (withLessons && moduleData.lessons.isNotEmpty()) {
            // By default, lessons are sorted by sequence number
            moduleData.lessons.forEachIndexed { lessonIndex, lesson ->
                val isLastLesson = lessonIndex == moduleData.lessons.size - 1
                val prefix = space + if (isLastLesson) "   └── " else "   ├── "
                print(
                    "$prefix ${lessonIndex + 1}. ${lesson.title} (${formatDurationMinutes(lesson.duration)})"
                )
                if (isReadMode) {
                    val status = if (recentLessonId < lesson.id) CompletionStatus.NOT_STARTED
                    else if (lesson.id < recentLessonId) CompletionStatus.COMPLETED
                    else recentLessonStatus
                    println("  Status: ${getCompletionText(status)}")
                } else {
                    println()
                }
            }
        } else if (withLessons)
            println("$space   └── No lessons available")
    }

    /**
     * Shows detailed information for a single lesson
     *
     * @param lesson The lesson to display
     * @param withResource Whether to include resource information
     */
    fun displayDetailedLesson(lesson: LessonData, withResource: Boolean = true) {
        val statusText = getStatusText(lesson.status)

        println("Title: ${lesson.title}")
        println("ID: ${lesson.id}")
        println("Duration: ${formatDurationMinutes(lesson.duration)}")
        println("Sequence: ${lesson.sequenceNumber}")
        println("Status: $statusText")

        if (withResource) println("Resource: ${lesson.resource}")
    }

    private fun getStatusText(status: ResourceStatus): String {
        return when (status) {
            ResourceStatus.DRAFT -> "Draft"
            ResourceStatus.PUBLISHED -> "Published"
            ResourceStatus.ARCHIVE -> "Archive"
        }
    }

    private fun getCompletionText(status: CompletionStatus): String {
        return when (status) {
            CompletionStatus.NOT_STARTED -> "Not Started ⛔️"
            CompletionStatus.IN_PROGRESS -> "In Progress ⏳"
            CompletionStatus.COMPLETED -> "Completed ✅"
        }
    }
}