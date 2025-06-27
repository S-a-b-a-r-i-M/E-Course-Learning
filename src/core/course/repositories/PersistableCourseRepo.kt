package core.course.repositories

import config.LogLevel
import config.logInfo
import core.course.schemas.CategoryData
import core.course.schemas.CourseLevel
import core.course.schemas.CourseType
import core.course.schemas.DetailedCourseData
import core.course.schemas.LessonData
import core.course.schemas.ModuleData
import core.course.schemas.NewCourseBasicData
import core.course.schemas.NewLessonData
import core.course.schemas.NewModuleData
import core.course.schemas.NewPriceData
import core.course.schemas.PriceDetailsData
import core.course.schemas.ResourceStatus
import core.course.schemas.UpdateCourseBasicData
import core.course.schemas.UpdateLessonData
import core.course.schemas.UpdateModuleData
import core.course.schemas.UpdatePriceDetailsData
import db.DatabaseManager
import utils.ErrorCode
import utils.Result
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

class PersistableCourseRepo : AbstractCourseRepo {
    private val conn
        get() = DatabaseManager.getDBConnection()

    // ******************* CREATE *******************
    override fun createCategory(name: String): CategoryData {
        val sql = "INSERT INTO category(name) VALUES (?) RETURNING id"

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.executeQuery().use { rs ->
                rs.next()
                return CategoryData(id = rs.getInt("id"), name)
            }
        }
    }

    override fun createLesson(newLessonData: NewLessonData, moduleId: Int): LessonData? {
        val sql = """
            INSERT INTO Lesson (title, resource, duration, status, module_id)
            VALUES (?, ?, ?, ?::ResourseStatus, ?)
            RETURNING id
        """.trimIndent()

        try {
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, newLessonData.title)
                pstmt.setString(2, newLessonData.resource)
                pstmt.setInt(3, newLessonData.duration)
                pstmt.setString(4, newLessonData.status.name)
                pstmt.setInt(5, moduleId)

                pstmt.executeQuery().use { rs ->
                    rs.next()
                    return LessonData.from(rs.getInt("id"), newLessonData)
                }
            }
        } catch (exp: Exception) {
            logInfo("${exp.message}", LogLevel.EXCEPTION)
            return null
        }
    }

    override fun createModule(newModuleData: NewModuleData, courseId: Int): ModuleData? {
        val sql = """
            INSERT INTO "Module" (title, description, duration, status, course_id)
            VALUES (?, ?, ?, ?::ResourseStatus, ?)
            RETURNING id
        """.trimIndent()

        try {
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, newModuleData.title)
                pstmt.setString(2, newModuleData.description)
                pstmt.setInt(3, 0)
                pstmt.setString(4, newModuleData.status.name)
                pstmt.setInt(5, courseId)

                pstmt.executeQuery().use { rs ->
                    rs.next()
                    return ModuleData.from(rs.getInt("id"),newModuleData)
                }
            }
        } catch (exp: Exception) {
            logInfo("${exp.message}", LogLevel.EXCEPTION)
            return null
        }
    }

    override fun createCourse(newCourseData: NewCourseBasicData, currentUserId: UUID): DetailedCourseData {
        val sql = """
            INSERT INTO Course (
                created_by,
                title,
                description,
                category,
                courseLevel,
                courseType,
                status,
                skills,
                prerequisites,
                isFreeCourse
            ) VALUES (?, ?, ?, ?, ?::CourseLevel, ?::CourseType, ?::ResourseStatus, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            // Add Values
            pstmt.setObject(1, currentUserId)
            pstmt.setString(2, newCourseData.title)
            pstmt.setString(3, newCourseData.description)
            pstmt.setString(4, newCourseData.category)
            pstmt.setString(5, newCourseData.courseLevel.name)
            pstmt.setString(6, newCourseData.courseType.name)
            pstmt.setString(7, newCourseData.status.name)
            pstmt.setArray(8, conn.createArrayOf(
                "varchar", newCourseData.skills.toTypedArray())
            )
            pstmt.setArray(9, conn.createArrayOf(
                "varchar", newCourseData.prerequisites?.toTypedArray())
            )
            pstmt.setBoolean(10, newCourseData.priceData != null)

            // Execute
            pstmt.executeQuery().use { rs ->
                rs.next()
                return DetailedCourseData.fromNewCourseBasicData(
                    rs.getInt("id"),
                    currentUserId,
                    newCourseData,
                )
            }
        }
    }

    override fun createPricing(newPriceData: NewPriceData, courseId: Int): PriceDetailsData {
        val sql = """
            INSERT INTO PriceDetails (currencyCode, currencySymbol, amount, course_id)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, newPriceData.currencyCode)
            pstmt.setString(2, newPriceData.currencySymbol)
            pstmt.setDouble(3, newPriceData.amount)
            pstmt.setInt(4, courseId)

            pstmt.executeQuery().use { rs ->
                rs.next()
                return PriceDetailsData.from(rs.getInt("id"), newPriceData)
            }
        }
    }

    // ******************* READ *******************
    private fun parseCategoryFromResult(rs: ResultSet) = if (rs.next()) {
        CategoryData(id=rs.getInt("id"), name=rs.getString("name"))
    } else null

    override fun getCategory(categoryId: Int): CategoryData? {
        val sql = "SELECT * FROM Category WHERE id=$categoryId"

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
               return parseCategoryFromResult(rs)
            }
        }
    }

    override fun getCategoryByName(name: String): CategoryData? {
        val sql = "SELECT * FROM Category WHERE name ILIKE ?"

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.executeQuery().use { rs ->
                return parseCategoryFromResult(rs)
            }
        }
    }


    private fun parseLessonFromResult(rs: ResultSet) = LessonData(
        id = rs.getInt("id"),
        title = rs.getString("title"),
        resource = rs.getString("resource"),
        duration = rs.getInt("duration"),
        status = ResourceStatus.getFromString(rs.getString("status")),
    )

    private fun getLessonsByModuleId(moduleId: Int): List<LessonData> {
        val sql = """SELECT * FROM lesson WHERE module_id=$moduleId ORDER BY id"""
        val lessons = mutableListOf<LessonData>()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next())
                    lessons.add(parseLessonFromResult(rs))

                return lessons
            }
        }
    }

    override fun getLesson(lessonId: Int): LessonData? {
        val sql = """SELECT * FROM lesson WHERE id=$lessonId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next())
                    return parseLessonFromResult(rs)

                logInfo("No lesson found ", LogLevel.EXCEPTION)
                return null
            }
        }
    }

    private fun parseModuleFromResult(rs: ResultSet, lessons: List<LessonData>) = ModuleData(
        id = rs.getInt("id"),
        title = rs.getString("title"),
        description = rs.getString("description"),
        duration = rs.getInt("duration"),
        status = ResourceStatus.getFromString(rs.getString("status")),
        lessons = lessons
    )

    private fun getModulesByCourseId(courseId: Int): List<ModuleData> {
        val sql = """SELECT * FROM "Module" WHERE course_id=$courseId ORDER BY id"""
        val modules: MutableList<ModuleData> = mutableListOf()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val moduleId = rs.getInt("id")
                    // Get All lessons
                    val lessons = getLessonsByModuleId(moduleId)

                    modules.add(parseModuleFromResult(rs, lessons))
                }

                return modules
            }
        }
    }

    override fun getModule(moduleId: Int): ModuleData? {
        val sql = """SELECT * FROM "Module" WHERE id=$moduleId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next()){
                    // Get All lessons
                    val moduleId = rs.getInt("id")
                    val lessons = getLessonsByModuleId(moduleId)

                    return parseModuleFromResult(rs, lessons)
                }

                return null
            }
        }
    }

    override fun getCourse(courseId: Int): DetailedCourseData? { // TODO: Need to improve query performance
        val sql = """SELECT * FROM course WHERE id=$courseId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (!rs.next()) {
                    logInfo("No course found for id($courseId)", LogLevel.EXCEPTION)
                    return null
                }

                // Get Module by Course id
                val modules = getModulesByCourseId(courseId)
                val priceDetails = getPriceDetails(courseId)

                return DetailedCourseData(
                    id = courseId,
                    createdBy = rs.getObject("created_by", UUID::class.java),
                    category = rs.getString("category"),
                    title = rs.getString("title"),
                    description = rs.getString("description"),
                    skills = rs.getArray("skills")
                        .array
                        .let { it as Array<String> }
                        .toList()
                    ,
                    duration = rs.getInt("duration"),
                    courseLevel = CourseLevel.getFromString(rs.getString("courseLevel")),
                    courseType = CourseType.getFromString(rs.getString("courseType")),
                    status = ResourceStatus.getFromString(rs.getString("status")),
                    prerequisites = rs.getArray("prerequisites")
                        ?.array
                        ?.let { it as Array<String> }
                        ?.toList(),
                    priceDetails = priceDetails,
                    modules = modules,
                )
            }
        }
    }

    override fun getPriceDetails(courseId: Int): PriceDetailsData? {
        val sql = """SELECT * FROM PriceDetails WHERE course_id=$courseId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next())
                    return PriceDetailsData(
                        id = rs.getInt("id"),
                        currencyCode = rs.getString("currencycode"),
                        currencySymbol = rs.getString("currencysymbol"),
                        amount = rs.getDouble("amount")
                    )

                return null
            }
        }
    }

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData> {
        var sql = "SELECT * FROM category"
        if (searchQuery.isNotEmpty()) sql += " WHERE name ILIKE '%$searchQuery%' "
        sql += " OFFSET $offset LIMIT $limit"
        val categories: MutableList<CategoryData> = mutableListOf()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next())
                    categories.add(
                        CategoryData(
                            id=rs.getInt("id"),
                            name=rs.getString("name")
                        )
                    )

                return categories
            }
        }
    }

    override fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        courseIds: List<Int>?
    ): List<DetailedCourseData> {
        var sql = "SELECT id FROM course"
        val whereQueries = mutableListOf<String>()
        // Apply Course ids
        if (courseIds != null && courseIds.isNotEmpty())
            whereQueries.add(" id IN (${courseIds.joinToString(",")}) ")
        // Apply Search
        if (searchQuery.isNotEmpty())
            whereQueries.add(" title ILIKE '%$searchQuery%' ")
        if (whereQueries.isNotEmpty())
            sql = sql + " WHERE ${whereQueries.joinToString(" and ")}"

        conn.createStatement().use { stmt ->
            stmt.executeQuery("$sql ORDER BY id OFFSET $offset LIMIT $limit"
            ).use { rs ->
                val courses = mutableListOf<DetailedCourseData>()
                while (rs.next()) {
                    val courseId = rs.getInt("id")
                    // Get specific course
                    val course = getCourse(courseId)
                    if (course != null)
                        courses.add(course)
                }

                return courses
            }
        }
    }

    override fun getCoursesByIds(courseIds: List<Int>): List<DetailedCourseData> {
        TODO("Not yet implemented")
    }

    // ******************* UPDATE *******************
    override fun updateOrCreatePricing(priceDetails: UpdatePriceDetailsData?, courseId: Int): Result<Unit> {
        if (priceDetails == null) {
            val sql = "DELETE FROM PriceDetails WHERE course_id=$courseId"
            conn.createStatement().use { stmt ->
                val count = stmt.executeUpdate(sql)
                return Result.Success(Unit, "Price details deleted successfully")
            }
        }
        val sql = if (priceDetails.id == 0) // Create
            """
            INSERT INTO PriceDetails(currencyCode, currencySymbol, amount, course_id) 
            VALUES (?, ?, ?, $courseId)
        """.trimIndent()
        else // Update
            """
            UPDATE PriceDetails
            SET currencycode=?, currencysymbol=?, amount=?
            WHERE id=${priceDetails.id}
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, priceDetails.currencyCode)
            pstmt.setString(2, priceDetails.currencySymbol)
            pstmt.setDouble(3, priceDetails.amount as Double)

            // Execute
            pstmt.executeUpdate()
            return Result.Success(Unit, "Price details updated/created successfully")
        }
    }

    override fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Result<Unit> {
        val sql = """
            UPDATE Course 
            SET
                title = COALESCE(?, title),
                description = COALESCE(?, description),
                status = COALESCE(?::ResourseStatus, status),
                skills = COALESCE(?, skills),
                prerequisites = COALESCE(?, prerequisites)
            WHERE id=?
        """.trimIndent()

        val affectedRows: Int = conn.prepareStatement(sql).use { pstmt ->
            // Add values
            pstmt.setString(1, updateData.title)
            pstmt.setString(2, updateData.description)
            pstmt.setString(3, updateData.status?.name)
            val skillsArr = updateData.skills?.let {
                conn.createArrayOf("varchar", updateData.skills?.toTypedArray())
            }
            pstmt.setArray(4, skillsArr)
            val requisitesArr = updateData.prerequisites?.let {
                conn.createArrayOf("varchar", updateData.prerequisites?.toTypedArray())
            }
            pstmt.setArray(5, requisitesArr)
            pstmt.setInt(6, courseId)

            // Execute
            pstmt.executeUpdate()
        }

        return if (affectedRows > 0)
            Result.Success(Unit, "Course($courseId) basic details updated successfully")
        else
            Result.Error("Course($courseId) is not found", ErrorCode.RESOURCE_NOT_FOUND)
    }

    override fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Result<Unit> {
        val sql = """
            UPDATE "Module" 
            SET
                title = COALESCE(?, title),
                description = COALESCE(?, description),
                status = COALESCE(?::ResourseStatus, status)
            WHERE id=?
        """.trimIndent()

        val affectedRows: Int = conn.prepareStatement(sql).use { pstmt ->
            // Add values
            pstmt.setString(1, updateData.title)
            pstmt.setString(2, updateData.description)
            pstmt.setString(3, updateData.status?.name)
            pstmt.setInt(4, moduleId)

            // Execute
            pstmt.executeUpdate()
        }

        return if (affectedRows > 0)
            Result.Success(Unit, "Module($moduleId) basic details updated successfully")
        else
            Result.Error("Module($moduleId) is not found", ErrorCode.RESOURCE_NOT_FOUND)
    }

    override fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData): Result<Unit> {
        val sql = """
            UPDATE Lesson 
            SET
                title = COALESCE(?, title),
                resource = COALESCE(?, resource),
                status = COALESCE(?::ResourseStatus, status),
                duration = COALESCE(?, duration)
            WHERE id=?
        """.trimIndent()

        val affectedRows: Int = conn.prepareStatement(sql).use { pstmt ->
            // Add values
            pstmt.setString(1, updateData.title)
            pstmt.setString(2, updateData.resource)
            pstmt.setString(3, updateData.status?.name)
            pstmt.setObject(4, updateData.newDuration, Types.INTEGER)
            pstmt.setInt(5, lessonId)

            // Execute
            pstmt.executeUpdate()
        }

        return if (affectedRows > 0)
            Result.Success(Unit, "Lesson($lessonId) details updated successfully")
        else
            Result.Error("Lesson($lessonId) is not found", ErrorCode.RESOURCE_NOT_FOUND)
    }

    override fun updateModuleDuration(moduleId: Int, duration: Int): Boolean {
        val sql = """
            UPDATE "Module" 
            SET duration=$duration + (SELECT duration FROM "Module" WHERE id=$moduleId)
            WHERE id=$moduleId
        """.trimIndent()

        conn.createStatement().use { stmt ->
            val count = stmt.executeUpdate(sql)
            return count != 0
        }
    }

    override fun updateCourseDuration(courseId: Int, duration: Int): Boolean {
        val sql = """UPDATE Course 
            |SET duration=$duration + (SELECT duration FROM Course WHERE id=$courseId) 
            |WHERE id=$courseId""".trimMargin()

        conn.createStatement().use { stmt ->
            val count = stmt.executeUpdate(sql)
            return count != 0
        }
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        val sql = """
            SELECT EXISTS(
                SELECT 1 FROM Category WHERE name=?
            )
        """.trimIndent()

        // Execute
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            return pstmt.executeQuery().use { rs ->
                rs.next()
                rs.getBoolean(1)
            }
        }
    }
}