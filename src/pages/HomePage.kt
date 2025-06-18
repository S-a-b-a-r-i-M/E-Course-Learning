package pages

import PageNames
import core.course.schemas.DetailedCourseData
import core.course.services.CourseService
import core.course.services.StudentCourseService
import core.user.schemas.UserData
import core.user.schemas.UserRole
import utils.displayCourse

class HomePage (val courseService: CourseService, val studentCourseService: StudentCourseService) {
    val editCoursePage = EditCoursePage(courseService)
    val coursePage: CoursePage = CoursePage(courseService, studentCourseService, editCoursePage)

    fun listCourses(pageTitle: PageNames, currentUser: UserData, onlyAssociated: Boolean = false) {
        var searchQuery = ""
        var offset = 0
        val limit = 10
        var hasMore = false

        // Decide User Type
        var isAdmin = false
        var isStudent = false
        if (currentUser.role == UserRole.ADMIN) isAdmin = true
        else if (currentUser.role == UserRole.STUDENT) isStudent = true

        fun fetchCourses(): List<DetailedCourseData> {
            val courses =
                courseService.getCourses(searchQuery, offset, limit, currentUser, onlyAssociated)
            if (courses.isEmpty()) {
                println("-------------- No Course to display -------------")
                hasMore = false
                return courses
            }
            courses.forEach { displayCourse(it) }
            hasMore = courses.size == limit
            return courses
        }
        // If no course available in initial fetch then return immediately
        if (fetchCourses().isEmpty())
            return

        while (true) {
            println("\n======== ${pageTitle.value} =========")
            println("\nOption to choose ⬇️")
            println("0 -> Go Back")
            println("1 -> Open a course")
            println("2 -> Search by Course name 🔍")
            if (hasMore) println("3 -> Load More ↻")
            print("Enter your option: ")
            val userInput = readln().toInt()

            when (userInput) {
                // Go Back
                0 -> break
                // Open a Course
                1 -> {
                    print("Enter course id: ")
                    val courseId = readln().toInt()
                    if (isStudent && PageNames.MY_COURSES == pageTitle) {
                        // Check if the entered course id is enrolled or not
                        if (
                            !studentCourseService.getEnrolledCourseIds(
                                currentUser.id
                            ).contains(courseId)
                        ) {
                            println("The selected course id is not yet enrolled by you.")
                            println("aborting to previous menu...")
                            continue
                        }
                    }
                    coursePage.openCourse(currentUser, courseId)
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
                    fetchCourses()
                }
                // Load More
                3 -> {
                    if (!hasMore) {
                        println("No more courses to load")
                        continue
                    }
                    offset += limit
                    fetchCourses()
                }

                else -> {
                    println("invalid option selected. Please try again.")
                }
            }
        }
    }

    fun start(currentUser: UserData) {
        val isAdmin = currentUser.role == UserRole.ADMIN
        val isStudent = currentUser.role == UserRole.STUDENT

        while (true) {
            println("\n======== ${PageNames.HOME_PAGE.value} =========")
            println("\nOption to choose ⬇️")
            println("0 -> Log out")
            println("1 -> List Of Courses")
            val option2 = if (isAdmin) PageNames.CREATE_COURSE else PageNames.MY_COURSES
            println("2 -> ${option2.value}")
            print("Enter your option: ")

            // When - Course Flow
            when (readln().toIntOrNull()) {
                0 -> break

                1 -> listCourses(PageNames.LIST_COURSES, currentUser)

                2 -> if (isAdmin) {
                    // Create Course
                    val course = coursePage.createCourse(currentUser)
                } else if (isStudent) {
                    // My Courses
                    listCourses(option2, currentUser, true)
                }
            }
        }
    }
}
