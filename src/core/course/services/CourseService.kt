package core.course.services

import config.LogLevel
import config.logInfo
import core.course.repositories.AbstractCourseRepo
import core.course.schemas.CategoryData
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import core.user.schemas.UserRole
import core.user.schemas.UserData
import utils.Result
import utils.hasPermission
import utils.hasPermissionV2
import kotlin.Int

class CourseService (
    private val courseRepo: AbstractCourseRepo,
    private val studentCourseService: StudentCourseService
) {
    /**
     * Retrieves detailed information for a specific course by its ID.
     *
     * @param courseId The unique identifier of the course.
     * @return A [DetailedCourseData] object containing full course information includes pricing, modules, etc.
     */
    fun getCourse(courseId: Int): DetailedCourseData? = courseRepo.getCourse(courseId)

    /**
     * Retrieves a paginated list of courses that match the given search query.
     *
     * @param searchQuery to search courses by title.
     * @param offset The starting index for pagination.
     * @param limit The maximum number of courses to return.
     * @return A list of [DetailedCourseData] objects.
     */
    fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        currentUser: UserData,
        onlyAssociated: Boolean = false,
    ): List<DetailedCourseData> {
        var courseIds: List<Int>? = null
        if (currentUser.role != UserRole.ADMIN && onlyAssociated) {
            if (currentUser.role == UserRole.STUDENT)
                // Get Student's Enrolled Course Ids
                courseIds = studentCourseService.getEnrolledCourseIds(currentUser.id)

            // If no course found then return immediately
            if (courseIds != null && courseIds.isEmpty()) {
                logInfo("No course found for user(${currentUser.fullName})", LogLevel.INFO)
                return emptyList()
            }
        }

        return courseRepo.getCourses(searchQuery, offset, limit, courseIds)
    }

    /**
     * Fetches a lesson.
     *
     * @param lessonId The lesson's primary id.
     * @return [LessonData] or null
     */
    fun getLesson(lessonId: Int) = courseRepo.getLesson(lessonId)

    /**
     * Fetches a module.
     *
     * @param moduleId The module's primary id.
     * @return [ModuleData] or null
     */
    fun getModule(moduleId: Int) = courseRepo.getModule(moduleId)

    /**
     * Fetches a price details.
     *
     * @param courseId The course's primary id.
     * @return [core.course.schemas.PriceDetailsData] or null
     */
    fun getCoursePriceDetails(courseId: Int) = courseRepo.getPriceDetails(courseId)

    /**
     * Fetches a paginated list of course categories, with an optional search filter.
     *
     * @param searchQuery The text to filter categories by name.
     * @param offset The starting index for pagination (e.g., 0 for the first page).
     * @param limit The maximum number of categories in one call.
     * @return A list of [CategoryData] matching the criteria.
     */
    fun getCategories(searchQuery: String, offset: Int, limit: Int) =
        courseRepo.getCategories(searchQuery, offset, limit)

    /**
     * Creates a new lesson within a specific module and updates the duration of the parent module and course.
     *
     * @param courseId The ID of the parent course, used to update its total duration.
     * @param moduleId The ID of the module to which this lesson will be added.
     * @return A [LessonData] object representing the newly created lesson.
     */
    fun createLesson(
        currentUser: UserData,
        courseId: Int,
        moduleId: Int,
        newLessonData: NewLessonData
    ):  LessonData? {
        if (!hasPermission(currentUser.role)) return null

        val lesson = courseRepo.createLesson(newLessonData, moduleId) ?: return null
        logInfo("New lesson created(id-${lesson.id})", LogLevel.INFO)
        // Update Durations in Module and Course
        courseRepo.updateModuleDuration(moduleId, lesson.duration)
        courseRepo.updateCourseDuration(courseId, lesson.duration)
        return lesson
    }

    /**
     * Creates a new module for a given course.
     *
     * @param courseId The ID of the course to which this module belongs.
     * @return A [ModuleData] object for the newly created module.
     */
    fun createModule(currentUser: UserData, courseId: Int, newModuleData: NewModuleData): ModuleData? {
        if (!hasPermission(currentUser.role)) return null

        return courseRepo.createModule(newModuleData, courseId)
    }

    /**
     * Creates a new course category.
     *
     * @param categoryName name of the category.
     * @return A [CategoryData] or null.
     */
    fun createCategory(currentUser: UserData, categoryName: String): CategoryData? {
        if (!hasPermission(currentUser.role)) return null

        return courseRepo.getCategoryByName(categoryName) ?: courseRepo.createCategory(categoryName)
    }

    /**
     * Orchestrates the end-to-end process of creating a new course.
     *
     * This function handles the entire course creation workflow:
     * 1. Checks if the `currentUser` has ADMIN permissions.
     * 2. Gathers basic course details from the user.
     * 3. Interactively prompts the user to create one or more modules with one or more lessons.
     *
     * @param currentUser The user attempting to create the course.
     * @return A [DetailedCourseData] the complete course object with all its modules
     *         and lessons, or `null` if the user does not have the required permissions.
     */
    fun createCourse(currentUser: UserData, newCourseData: NewCourseBasicData): DetailedCourseData? {
        if (!hasPermission(currentUser.role)) return null

        // Create course with basic details
        val course = courseRepo.createCourse(newCourseData, currentUser.id)

        // Attach PriceDetails to Course
        val courseId = course.id
        if (newCourseData.priceData != null)
            courseRepo.createPricing(newCourseData.priceData, courseId)
        logInfo(
            "${newCourseData.title}(id-${courseId}) created successfully with basic details",
            LogLevel.INFO
        )
        return course
    }

    /**
     * Updates the basic details of a specific course.
     *
     * @param courseId The unique identifier of the course to be updated.
     * @param updateData An [UpdateCourseBasicData] object containing the fields to update.
     */
    fun updateCourseBasicDetails(currentUser: UserData, courseId: Int, updateData: UpdateCourseBasicData) {
        if (!hasPermission(currentUser.role)) return

        courseRepo.updateCourseBasicDetails(courseId, updateData)
        logInfo("Course($courseId) basic details updated.", LogLevel.INFO)
    }

    /**
     * Updates or creates the pricing details for a specific course.
     *
     * This function can be used to change the price of a course, or to add pricing
     *
     * @param courseId The unique identifier of the course whose pricing will be updated.
     * @param priceDetails An [UpdatePriceDetailsData] object with the new pricing information,
     * or `null` to remove existing pricing details (e.g., making a course free).
     */
    fun updateCoursePricing(currentUser: UserData, courseId: Int, priceDetails: UpdatePriceDetailsData?): Result<Unit> {
        return when (val permissionResult = hasPermissionV2(currentUser.role)) {
            is Result.Error -> permissionResult
            is Result.Success -> courseRepo.updateOrCreatePricing(priceDetails, courseId)
        }
    }

    /**
     * Updates the details of a specific module.
     *
     * @param moduleId The unique identifier of the module to be updated.
     * @param updateData An [UpdateModuleData] object containing the new data for the module.
     */
    fun updateModuleDetails(currentUser: UserData, moduleId: Int, updateData: UpdateModuleData): Result<Unit> {
        return when (val permissionResult = hasPermissionV2(currentUser.role)) {
            is Result.Error -> permissionResult
            is Result.Success -> courseRepo.updateModuleDetails(moduleId, updateData)
        }
    }

    /**
     * Updates the details of a specific lesson.
     *
     * @param lessonId The unique identifier of the lesson to be updated.
     * @param updateData An [UpdateLessonData] object containing the new data for the lesson.
     */
    fun updateLessonDetails(currentUser: UserData, courseId: Int, moduleId: Int, lessonId: Int, updateData: UpdateLessonData): Result<Unit> {
        return when (val permissionResult = hasPermissionV2(currentUser.role)) {
            is Result.Error -> permissionResult
            is Result.Success -> {
                val result = courseRepo.updateLessonDetails(lessonId, updateData)
                if (updateData.newDuration != null && updateData.oldDuration != null) {
                    val durationDifference = updateData.newDuration!! - updateData.oldDuration!!
                    courseRepo.updateModuleDuration(moduleId, durationDifference)
                    courseRepo.updateCourseDuration(courseId, durationDifference)
                    logInfo(
                        "Lesson($lessonId) related module and course duration also updated",
                        LogLevel.INFO
                    )
                }
                return result
            }
        }
    }
}