package core.course.repositories

import config.LogLevel
import config.logInfo
import core.auth.services.CURRENT_FILE_NAME
import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.schemas.PriceDetailsData
import core.course.schemas.CourseLevel
import core.course.schemas.CourseType
import core.course.schemas.ResourceStatus
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import utils.ErrorCode
import utils.Result
import java.util.UUID
import kotlin.math.abs

val CURRENT_FILE_NAME: String = Throwable().stackTrace[0].fileName ?: ""

class CourseRepo : AbstractCourseRepo {
    companion object {
        // Serial Ids
        private var categorySerialId = 1
        private var priceDetailsSerialId = 1
        private var courseSerialId = 1
        private var moduleSerialId = 1
        private var lessonSerialId = 1

        private fun getNextCategoryId() = categorySerialId++
        private fun getNextPriceDetailsId() = priceDetailsSerialId++
        private fun getNextCourseId() = courseSerialId++
        private fun getNextModuleId() = moduleSerialId++
        private fun getNextLessonId() = lessonSerialId++

        // Storage
        private val courseRecords = mutableMapOf<Int, DetailedCourseData>()
        private val categoryRecords = mutableMapOf<Int, CategoryData>()

        private val moduleIdToCourseId = mutableMapOf<Int, Int>()
        private val lessonIdToModuleId = mutableMapOf<Int, Int>()
    }
    // ******************* CREATE *******************
    override fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData {
        val course = DetailedCourseData(
            id = getNextCourseId(),
            title = newCourseData.title,
            description = newCourseData.description,
            createdBy = currentUserId,
            skills = newCourseData.skills,
            courseLevel = newCourseData.courseLevel,
            courseType = newCourseData.courseType,
            status = ResourceStatus.DRAFT,
            prerequisites = newCourseData.prerequisites,
            category = newCourseData.category
        )

        courseRecords[course.id] = course
        return course
    }

    override fun createCategory(name: String): CategoryData {
        val category = CategoryData(getNextCategoryId(), name)
        categoryRecords[category.id] = category
        return category
    }

    override fun createPricing(newPriceData: NewPriceData, courseId: Int): PriceDetailsData {
        val course = courseRecords.getValue(courseId)
        // Add price-details id into course
        val priceDetails = PriceDetailsData(
            id = getNextPriceDetailsId(),
            currencyCode = newPriceData.currencyCode,
            currencySymbol = newPriceData.currencySymbol,
            amount = newPriceData.amount
        )
        courseRecords[courseId] = course.copy(priceDetails=priceDetails)
        return priceDetails
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData? {
        val course = courseRecords[courseId]
        if (course == null) {
            println("There is no course found for courseId($courseId)")
            return null
        }

        val newModule = ModuleData(
            id = getNextModuleId(),
            title = newModuleData.title,
            description = newModuleData.description,
            status = newModuleData.status,
        )

        // Store
        val updatedModules = course.modules.toMutableList()
        updatedModules.add(newModule)
        courseRecords[course.id] = course.copy(modules = updatedModules)
        moduleIdToCourseId[newModule.id] = courseId
        return newModule
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData? {
        val courseId = moduleIdToCourseId[moduleId]
        if (courseId == null) {
            println("There is no course reference found for moduleId($moduleId)")
            return null
        }

        val newLesson = LessonData (
            id = getNextLessonId(),
            title = newLessonData.title,
            resource = newLessonData.resource,
            duration = newLessonData.duration,
            status = newLessonData.status,
        )

        // Store
        val course = courseRecords.getValue(courseId)
        val updatedModules = course.modules.map { module ->
            if (module.id == moduleId) {
                // Add new lesson to existing lessons
                val updatedLessons = module.lessons + newLesson
                module.copy(lessons = updatedLessons)
            } else
                module
        }

        courseRecords[courseId] = course.copy(modules = updatedModules)
        lessonIdToModuleId[newLesson.id] = moduleId
        return newLesson
    }

    // ******************* READ *********************
    override fun getCategory(categoryId: Int): CategoryData? {
        val category = categoryRecords[categoryId]
        if (category == null)
            println("Course not available for courseId($categoryId)")

        return category
    }

    override fun getCourse(courseId: Int): DetailedCourseData? {
        val course = courseRecords[courseId]
        if (course == null)
            println("Course not available for courseId($courseId)")

        return course
    }

    fun getCourseV2(courseId: Int): Result<DetailedCourseData> {
        val course = courseRecords[courseId]
        if (course == null) {
            logInfo("Course not available for courseId($courseId)", LogLevel.EXCEPTION, CURRENT_FILE_NAME
            )
            return Result.Error("Course not available for courseId($courseId)", ErrorCode.RESOURCE_NOT_FOUND)
        }

        return Result.Success(course)
    }

    override fun getPriceDetails(courseId: Int): PriceDetailsData? {
        val course = courseRecords[courseId]
        if (course == null) {
            println("Course not available for courseId($courseId)")
            return null
        }

        return course.priceDetails
    }

    private fun getCourseByModuleId(moduleId: Int): DetailedCourseData? {
        val courseId = moduleIdToCourseId[moduleId]
        if (courseId == null) {
            println("Course reference is not available fof moduleId($moduleId)")
            return null
        }
        return courseRecords.getValue(courseId)
    }

    private fun getCourseByModuleIdV2(moduleId: Int): Result<DetailedCourseData> {
        val courseId = moduleIdToCourseId[moduleId]
        if (courseId == null) {
            logInfo("Course reference is not available fof moduleId($moduleId)", LogLevel.EXCEPTION, CURRENT_FILE_NAME)
            return Result.Error(
                "Course reference is not available fof moduleId($moduleId)",
                ErrorCode.RESOURCE_NOT_FOUND
            )
        }
        return Result.Success(courseRecords.getValue(courseId))
    }

    private fun getModuleIdFromLessonId(lessonId: Int): Result<Int> {
        val moduleId = lessonIdToModuleId[lessonId]
        if (moduleId == null) {
            return Result.Error(
                "Module reference is not available for lessonId($lessonId)",
                ErrorCode.RESOURCE_NOT_FOUND
            )
        }

        return Result.Success(moduleId)
    }

    override fun getModule(moduleId: Int): ModuleData? {
        val course = getCourseByModuleId(moduleId) ?: return null
        return course.modules.find { it.id == moduleId }
    }

    override fun getLesson(lessonId: Int): LessonData? {
        val moduleId = lessonIdToModuleId[lessonId]
        if (moduleId == null) {
            println("Module reference is not available for lessonId($lessonId)")
            return null
        }

        val course = getCourseByModuleId(moduleId) ?: return null
        return course.modules.find { it.id == moduleId }?.lessons?.find { it.id == lessonId }
    }

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData> {
        val endIndex = (offset + 1) * limit
        val categories = categoryRecords.values.toList()

        // Apply Search
        val result = categories.filter { it.name.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        courseIds: List<Int>?
    ): List<DetailedCourseData> {
        val endIndex = (offset + 1) * limit
        val courses = courseRecords.values.toList()
        var result: List<DetailedCourseData> = courses

        // Apply Course ids
        if (courseIds != null && courseIds.isNotEmpty()) {
            val courseIdsSet = courseIds.toSet()
            result = courses.filter { courseIdsSet.contains(it.id) }
        }
        // Apply Search
        result = result.filter { it.title.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCoursesByIds(courseIds: List<Int>): List<DetailedCourseData> {
        return courseIds.map { courseRecords.getValue(it) }
    }

    // ******************* UPDATE *******************
    override fun updateOrCreatePricing(priceDetails: UpdatePriceDetailsData?, courseId: Int): Result<Unit> {
        val course = courseRecords[courseId]
        if (course == null) {
            logInfo("There is no course found for courseId($courseId)", LogLevel.EXCEPTION, CURRENT_FILE_NAME
            )
            return Result.Error(
                "There is no course found for courseId($courseId)",
                ErrorCode.RESOURCE_NOT_FOUND
            )
        }

        val finalPriceDetails = if (priceDetails == null)
            null
        else if (priceDetails.id == 0)
            // Create
            PriceDetailsData(
                id = getNextPriceDetailsId(),
                currencyCode = priceDetails.currencyCode as String,
                currencySymbol = priceDetails.currencySymbol as String,
                amount = priceDetails.amount as Double,
            )
        else // Update
            PriceDetailsData(
                id = priceDetails.id,
                currencyCode = priceDetails.currencyCode as String,
                currencySymbol = priceDetails.currencySymbol as String,
                amount = priceDetails.amount as Double,
            )

        courseRecords[courseId] = course.copy(priceDetails=finalPriceDetails)
        return Result.Success<Unit>(Unit, "Price details updated successfully")
    }

    override fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Result<Unit> {
        return when (val courseResult = getCourseV2(courseId)) {
            is Result.Error -> courseResult
            is Result.Success -> {
                val course = courseResult.data
                courseRecords[courseId] = course.copy(
                    title = updateData.title ?: course.title,
                    description = updateData.description ?: course.description,
                    skills = updateData.skills ?: course.skills,
                    prerequisites = updateData.prerequisites ?: course.prerequisites,
                    status = updateData.status ?: course.status,
                )
                Result.Success(Unit, "Course($courseId) basic details updated successfully")
            }
        }
    }

    override fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Result<Unit> {
        return when (val courseResult = getCourseByModuleIdV2(moduleId)) {
            is Result.Error -> courseResult
            is Result.Success -> {
                val course = courseResult.data
                val updatedModules = course.modules.map { module ->
                    if (module.id == moduleId)
                        module.copy(
                            title = updateData.title ?: module.title,
                            description = updateData.description ?: module.description,
                            status = updateData.status ?: module.status
                        )
                    else
                        module
                }

                // Store
                courseRecords[course.id] = course.copy(modules = updatedModules)
                Result.Success(Unit)
            }
        }
    }

    override fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData): Result<Unit> {
        return when (val result = getModuleIdFromLessonId(lessonId)) {
            is Result.Error -> {
                logInfo(result.message, LogLevel.EXCEPTION, CURRENT_FILE_NAME)
                Result.Error("Lesson($lessonId) is not found", ErrorCode.RESOURCE_NOT_FOUND)
            }
            is Result.Success -> {
                val moduleId = result.data
                val courseId = moduleIdToCourseId.getValue(moduleId)
                val course = courseRecords.getValue(courseId)

                val updatedModules = course.modules.map { module ->
                    if (module.id == moduleId) {
                        val updatedLessons = module.lessons.map { lesson ->
                            if (lesson.id == lessonId) {
                                lesson.copy(
                                    title = updateData.title ?: lesson.title,
                                    resource = updateData.resource ?: lesson.resource,
                                    duration = updateData.duration ?: lesson.duration,
                                    status = updateData.status ?: lesson.status
                                )
                            } else
                                lesson
                        }
                        module.copy(lessons = updatedLessons)
                    } else
                        module
                }

                // Store
                courseRecords[course.id] = course.copy(modules = updatedModules)
                Result.Success(Unit, "Lesson($lessonId) updated successfully.")
            }
        }
    }

    override fun updateModuleDuration(moduleId: Int, duration: Int): Boolean {
        val course = getCourseByModuleId(moduleId)
        if (course == null)
            return false

        val updatedModules = course.modules.map { module ->
            if(module.id == moduleId)
                module.copy(duration = abs(module.duration + duration))
            else
                module
        }

        // Store
        courseRecords[course.id] = course.copy(modules = updatedModules)
        return true
    }

    override fun updateCourseDuration(courseId: Int, duration: Int): Boolean {
        val course = courseRecords[courseId]
        if (course == null) {
            println("Course not available for courseId($courseId)")
            return false
        }
        courseRecords[courseId] = course.copy(duration = abs(course.duration + duration))
        return true
    }

    // ******************* DELETE *******************

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String) = categoryRecords.values.any { it -> it.name == name }

    // ******************* HELPER *******************

    // TODO: Move these data to separate JSON files
    init {
        listOf(
            CategoryData(id = 1, name = "Web Development"),
            CategoryData(id = 2, name = "Data Science"),
            CategoryData(id = 3, name = "Mobile Development"),
            CategoryData(id = 4, name = "Cloud Computing"),
            CategoryData(id = 5, name = "Cybersecurity"),
            CategoryData(id = 6, name = "Digital Marketing"),
            CategoryData(id = 7, name = "Personal Growth")
        ).forEach { categoryRecords[it.id] = it }

    // Create Course
        // Module 1: C++ Fundamentals
        val module1 = ModuleData(
            id = getNextModuleId(),
            title = "C++ Fundamentals",
            description = "Learn the basics of C++ programming language",
            duration = 240, // 4 hours
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Introduction to C++",
                    resource = "cpp_intro_video.mp4",
                    duration = 30,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Setting up Development Environment",
                    resource = "setup_guide.pdf",
                    duration = 25,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Variables and Data Types",
                    resource = "variables_tutorial.mp4",
                    duration = 45,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Input/Output Operations",
                    resource = "io_operations.cpp",
                    duration = 35,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Control Structures",
                    resource = "control_structures.mp4",
                    duration = 50,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Practice Exercises - Basics",
                    resource = "basic_exercises.pdf",
                    duration = 55,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )
        // Module 2: Functions and Arrays
        val module2 = ModuleData(
            id = getNextModuleId(),
            title = "Functions and Arrays",
            description = "Master functions, arrays, and memory management",
            duration = 185,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Function Basics",
                    resource = "functions_intro.mp4",
                    duration = 40,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Function Overloading",
                    resource = "function_overloading.cpp",
                    duration = 35,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Arrays and Pointers",
                    resource = "arrays_pointers.mp4",
                    duration = 60,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Dynamic Memory Allocation",
                    resource = "memory_management.pdf",
                    duration = 50,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )

        val course1 = DetailedCourseData(
            id = getNextCourseId(),
            createdBy = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            category = "Programming",
            title = "Complete C++ Programming Masterclass",
            description = "Master C++ programming from basics to advanced concepts. Build real-world applications and gain industry-ready skills in one of the most powerful programming languages.",
            skills = listOf(
                "C++ Programming",
                "Object-Oriented Programming",
                "Memory Management",
                "Data Structures",
                "Algorithm Implementation",
                "Problem Solving",
                "Software Development"
            ),
            duration = 425,
            courseLevel = CourseLevel.INTERMEDIATE,
            courseType = CourseType.SELF_PACED,
            status = ResourceStatus.PUBLISHED,
            prerequisites = listOf(
                "Basic computer knowledge",
                "Understanding of programming concepts",
                "Mathematics fundamentals"
            ),
            priceDetails = PriceDetailsData(getNextPriceDetailsId(), "USD", "$", 99.0),
            modules = mutableListOf(module1, module2)
        )
        // Module 1: Kitchen Basics
        val module5 = ModuleData(
            id = getNextModuleId(),
            title = "Kitchen Basics & Safety",
            description = "Learn essential kitchen skills, safety, and basic cooking techniques",
            duration = 45,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Kitchen Safety and Hygiene",
                    resource = "kitchen_safety.mp4",
                    duration = 25,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Essential Kitchen Tools",
                    resource = "kitchen_tools_guide.pdf",
                    duration = 20,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )

        // Module 2: Fundamental Recipes
        val module6 = ModuleData(
            id = getNextModuleId(),
            title = "Fundamental Recipes",
            description = "Master basic recipes that form the foundation of cooking",
            duration = 75, // 4 hours
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Perfect Rice and Grains",
                    resource = "rice_grains_tutorial.mp4",
                    duration = 30,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Basic Pasta Dishes",
                    resource = "pasta_basics.mp4",
                    duration = 45,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )

        val course2 = DetailedCourseData(
            id = getNextCourseId(),
            createdBy = UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
            category = "Culinary Arts",
            title = "Home Cooking Mastery: From Basics to Gourmet",
            description = "Transform your cooking skills from beginner to confident home chef. Learn essential techniques, international cuisines, and create restaurant-quality meals at home.",
            skills = listOf(
                "Basic Cooking Techniques",
                "Knife Skills",
                "International Cuisine",
                "Baking and Desserts",
                "Meal Planning",
                "Kitchen Safety",
                "Food Presentation",
                "Flavor Pairing"
            ),
            duration = 120,
            courseLevel = CourseLevel.BEGINNER,
            courseType = CourseType.SELF_PACED,
            status = ResourceStatus.PUBLISHED,
            prerequisites = listOf(
                "Access to a basic kitchen",
                "Willingness to practice",
                "Basic reading skills for recipes"
            ),
            priceDetails = PriceDetailsData(getNextPriceDetailsId(), "USD", "$", 49.0),
            modules = mutableListOf(module5, module6)
        )

        courseRecords[course1.id] = course1
        courseRecords[course2.id] = course2
        listOf(course1, course2).forEach { course ->
            course.modules.forEach { module ->
                moduleIdToCourseId[module.id] = course1.id
                module.lessons.forEach { lesson ->
                    lessonIdToModuleId[lesson.id] = module.id
                }
            }
        }
    }
}
