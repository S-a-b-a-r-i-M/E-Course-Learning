package core.course.repositories

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
import java.util.UUID

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

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
        private val moduleRecords = mutableMapOf<Int, ModuleData>()
        private val lessonRecords = mutableMapOf<Int, LessonData>()
        private val categoryRecords = mutableMapOf<Int, CategoryData>()

        private val priceDetailsToCourseId = mutableMapOf<Int, Int>()
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
        println("$CURRENT_FILE_NAME: New course created")
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
        priceDetailsToCourseId[priceDetails.id] = course.id
        return priceDetails
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData {
        val course = courseRecords.getValue(courseId)

        val module = ModuleData(
            id = getNextModuleId(),
            title = newModuleData.title,
            description = newModuleData.description,
            sequenceNumber = newModuleData.sequenceNumber,
            status = newModuleData.status,
        )
        println("$CURRENT_FILE_NAME: New module created(id-${module.id})")

        // Add module into maps
        course.modules.add(module)
        moduleRecords[module.id] = module
        moduleIdToCourseId[module.id] = courseId

        return module
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData {
        val module = moduleRecords.getValue(moduleId)
        val lesson = LessonData (
            id = getNextLessonId(),
            title = newLessonData.title,
            resource = newLessonData.resource,
            duration = newLessonData.duration,
            sequenceNumber = newLessonData.sequenceNumber,
            status = newLessonData.status,
        )
        println("$CURRENT_FILE_NAME: New lesson created(id-${lesson.id})")

        // Add lesson into maps
        module.lessons.add(lesson)
        lessonRecords[lesson.id] = lesson
        lessonIdToModuleId[lesson.id] = moduleId

        return lesson
    }

    // ******************* READ *********************
    override fun getCategory(categoryId: Int): CategoryData? {
        val category = categoryRecords[categoryId]
        if (category == null)
            println("$CURRENT_FILE_NAME: Course not available fo courseId($categoryId)")

        return category
    }

    override fun getCourse(courseId: Int): DetailedCourseData? {
        val course = courseRecords[courseId]
        if (course == null)
            println("$CURRENT_FILE_NAME: Course not available fo courseId($courseId)")

        return course
    }

    override fun getPriceDetails(courseId: Int): PriceDetailsData? {
        val course = courseRecords[courseId]
        if (course == null)
            println("$CURRENT_FILE_NAME: Course not available fo courseId($courseId)")

        return courseRecords.getValue(courseId).priceDetails
    }

    override fun getModule(moduleId: Int): ModuleData? {
        val module = moduleRecords[moduleId]
        if (module == null)
            println("$CURRENT_FILE_NAME: Module not available fo moduleId($moduleId)")

        return module
    }

    override fun getLesson(lessonId: Int): LessonData? {
        val lesson = lessonRecords[lessonId]
        if (lesson == null)
            println("$CURRENT_FILE_NAME: Lesson not available fo lessonId($lessonId)")

        return lesson
    }

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData> {
        val endIndex = (offset + 1) * limit
        val categories = categoryRecords.values.toList()

        // Apply Search
        val result = categories.filter { it.name.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCourses(searchQuery: String, offset: Int, limit: Int): List<DetailedCourseData> {
        val endIndex = (offset + 1) * limit
        val courses = courseRecords.values.toList()

        // Apply Search
        val result = courses.filter { it.title.contains(searchQuery, true) }
        // Apply Pagination
        return result.subList(offset, endIndex.coerceAtMost(result.size))
    }

    override fun getCoursesByIds(courseIds: List<Int>): List<DetailedCourseData> {
        return courseIds.map { courseRecords.getValue(it) }
    }

    // ******************* UPDATE *******************
    override fun updateOrCreatePricing(priceDetails: UpdatePriceDetailsData?, courseId: Int): Boolean {
        val course = courseRecords.getValue(courseId)
        val existingPriceDetails = course.priceDetails

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
        return true
    }

    override fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Boolean {
        val course = courseRecords.getValue(courseId)

        courseRecords[courseId] = course.copy(
            title = updateData.title ?: course.title,
            description = updateData.description ?: course.description,
            skills = updateData.skills ?: course.skills,
            prerequisites = updateData.prerequisites ?: course.prerequisites,
            status = updateData.status ?: course.status,
        )
        return true
    }

    override fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Boolean {
        val courseId = moduleIdToCourseId.getValue(moduleId)
        val course = courseRecords.getValue(courseId)
        val moduleIndex = course.modules.indexOfFirst { it.id == moduleId }
        if (moduleIndex == -1) return false // Return if not found

        val module = course.modules[moduleIndex]
        val updatedModule = module.copy(
            title = updateData.title ?: module.title,
            description = updateData.description ?: module.description,
            status = updateData.status ?: module.status
        )
        course.modules[moduleIndex] = updatedModule
        moduleRecords[moduleId] = updatedModule
        return true
    }

    override fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData): Boolean {
        val moduleId = lessonIdToModuleId.getValue(lessonId)
        val module = moduleRecords.getValue(moduleId)

        for((index, lesson) in module.lessons.withIndex()) {
            if (lesson.id != lessonId) continue

            val updatedLesson = lesson.copy(
                title = updateData.title ?: lesson.title,
                resource = updateData.resource ?: lesson.resource,
                duration = updateData.duration ?: lesson.duration,
                status = updateData.status ?: lesson.status
            )
            module.lessons[index] = updatedLesson
            lessonRecords[lessonId] = updatedLesson
            return true
        }

        return false
    }

    // ******************* DELETE *******************
    override fun deleteLesson(lessonId: Int): Boolean {
//        val moduleId = lessonIdToModuleId.getValue(lessonId)
//        val module = moduleRecords.getValue(moduleId)
//
//        lessonIdToModuleId.remove(lessonId)
//        lessonRecords.remove(lessonId)
//        return module.lessons.removeIf { it.id == lessonId }
        TODO("Not yet implemented")
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String) = categoryRecords.values.any { it -> it.name == name }

    fun isCourseIdExists(courseId: Int): Boolean = courseId in courseRecords.keys

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
            sequenceNumber = 1,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Introduction to C++",
                    resource = "cpp_intro_video.mp4",
                    duration = 30,
                    sequenceNumber = 1,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Setting up Development Environment",
                    resource = "setup_guide.pdf",
                    duration = 25,
                    sequenceNumber = 2,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Variables and Data Types",
                    resource = "variables_tutorial.mp4",
                    duration = 45,
                    sequenceNumber = 3,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Input/Output Operations",
                    resource = "io_operations.cpp",
                    duration = 35,
                    sequenceNumber = 4,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Control Structures",
                    resource = "control_structures.mp4",
                    duration = 50,
                    sequenceNumber = 5,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Practice Exercises - Basics",
                    resource = "basic_exercises.pdf",
                    duration = 55,
                    sequenceNumber = 6,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )

        // Module 2: Functions and Arrays
        val module2 = ModuleData(
            id = getNextModuleId(),
            title = "Functions and Arrays",
            description = "Master functions, arrays, and memory management",
            duration = 300, // 5 hours
            sequenceNumber = 2,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Function Basics",
                    resource = "functions_intro.mp4",
                    duration = 40,
                    sequenceNumber = 1,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Function Overloading",
                    resource = "function_overloading.cpp",
                    duration = 35,
                    sequenceNumber = 2,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Arrays and Pointers",
                    resource = "arrays_pointers.mp4",
                    duration = 60,
                    sequenceNumber = 3,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Dynamic Memory Allocation",
                    resource = "memory_management.pdf",
                    duration = 50,
                    sequenceNumber = 4,
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
            duration = 180, // 3 hours
            sequenceNumber = 1,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Kitchen Safety and Hygiene",
                    resource = "kitchen_safety.mp4",
                    duration = 25,
                    sequenceNumber = 1,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Essential Kitchen Tools",
                    resource = "kitchen_tools_guide.pdf",
                    duration = 20,
                    sequenceNumber = 2,
                    status = ResourceStatus.PUBLISHED
                )
            )
        )

        // Module 2: Fundamental Recipes
        val module6 = ModuleData(
            id = getNextModuleId(),
            title = "Fundamental Recipes",
            description = "Master basic recipes that form the foundation of cooking",
            duration = 240, // 4 hours
            sequenceNumber = 2,
            status = ResourceStatus.PUBLISHED,
            lessons = mutableListOf(
                LessonData(
                    id = getNextLessonId(),
                    title = "Perfect Rice and Grains",
                    resource = "rice_grains_tutorial.mp4",
                    duration = 30,
                    sequenceNumber = 1,
                    status = ResourceStatus.PUBLISHED
                ),
                LessonData(
                    id = getNextLessonId(),
                    title = "Basic Pasta Dishes",
                    resource = "pasta_basics.mp4",
                    duration = 45,
                    sequenceNumber = 2,
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
                moduleRecords[module.id] = module
                moduleIdToCourseId[module.id] = course1.id
                module.lessons.forEach { lesson ->
                    lessonRecords[lesson.id] = lesson
                    lessonIdToModuleId[lesson.id] = module.id
                }
            }
        }
    }
}
