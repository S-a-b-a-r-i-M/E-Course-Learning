package core.course.repositories

import config.LogLevel
import config.logInfo
import core.course.schemas.CategoryData
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
import utils.Result
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID

class PersistableCourseRepo : AbstractCourseRepo {
    // ******************* CREATE *******************
    private val conn
        get() = DatabaseManager.getDBConnection()

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
            INSERT INTO Lesson (title, resourse, duration, status, module_id)
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
            INSERT INTO "Module" (title, description, duration, status, courseId)
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
                createdBy,
                title,
                description,
                category,
                courseLevel,
                courseType,
                staus,
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
            INSERT INTO PriceDetails (currencyCode, currencySymbol, amount, courseId)
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
    override fun getCategory(categoryId: Int): CategoryData? {
        val sql = "SELECT * FROM Category WHERE id=$categoryId"

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next()){
                   return CategoryData(
                       id=rs.getInt("id"),
                       name=rs.getString("name")
                   )
                }

                return null
            }
        }
    }

    private fun parseLessonFromResult(rs: ResultSet) = LessonData(
        id = rs.getInt("id"),
        title = rs.getString("title"),
        resource = rs.getString("resource"),
        duration = rs.getInt("duration"),
        status = ResourceStatus.getFromStrValue(rs.getString("status")),
    )

    private fun getLessonsByModuleId(moduleId: Int): List<LessonData> {
        val sql = """SELECT * FROM lesson WHERE module_id=$moduleId"""
        val lessons = mutableListOf<LessonData>()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next())
                    lessons.add(parseLessonFromResult(rs))

                return lessons
            }
        }
    }

    private fun getModulesByCourseId(courseId: Int): List<ModuleData> {
        val sql = """SELECT * FROM "Module" WHERE course_id=$courseId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next()){
                    getModule()
                    // Get All lessons
                    val moduleId = rs.getInt("id")
                    val lessons = getLessonsByModuleId(moduleId)

                    return ModuleData(
                        id = rs.getInt("id"),
                        title = rs.getString("title"),
                        description = rs.getString("description"),
                        duration = rs.getInt("duration"),
                        status = ResourceStatus.getFromStrValue(rs.getString("status")),
                        lessons = lessons
                    )
                }

                return null
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

                    return ModuleData(
                        id = rs.getInt("id"),
                        title = rs.getString("title"),
                        description = rs.getString("description"),
                        duration = rs.getInt("duration"),
                        status = ResourceStatus.getFromStrValue(rs.getString("status")),
                        lessons = lessons
                    )
                }

                return null
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

    override fun getCourse(courseId: Int): DetailedCourseData? {
        val sql = """SELECT * FROM course WHERE id=$courseId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (!rs.next()) {
                    logInfo("No lesson found ", LogLevel.EXCEPTION)
                    return null
                }

                // Get Module by Course Id
//                getModule()
            }
        }

        return null
    }

    override fun getPriceDetails(courseId: Int): PriceDetailsData? {
        val sql = """SELECT * FROM PriceDetails WHERE id=$courseId"""

        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                if (rs.next())
                    return PriceDetailsData(
                        id = rs.getInt("id"),
                        currencyCode = rs.getString("currencycode"),
                        currencySymbol = rs.getString("currencysymbol"),
                        amount = rs.getDouble("amount")
                    )

                logInfo("No PriceDetails found for Course($courseId)", LogLevel.EXCEPTION)
                return null
            }
        }

    }

    override fun getCategories(searchQuery: String, offset: Int, limit: Int): List<CategoryData> {
        return listOf(CategoryData(1, "development"))
    }

    override fun getCourses(
        searchQuery: String,
        offset: Int,
        limit: Int,
        courseIds: List<Int>?
    ): List<DetailedCourseData> {
        TODO("Not yet implemented")
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
            INSERT INTO PriceDetails(currencyCode, currencySymbol, amount) 
            VALUES (?, ?, ?)
        """.trimIndent()
        else // Update
            """
            UPDATE PriceDetails
            SET currencycode=?, currencysymbol=?, amount=?
            WHERE id=${priceDetails.id}
        """.trimIndent()

        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, priceDetails.currencyCode)
            pstmt.setString(1, priceDetails.currencySymbol)
            pstmt.setDouble(1, priceDetails.amount as Double)

            // Execute
            pstmt.executeUpdate()
            return Result.Success(Unit, "Price details updated/created successfully")
        }
    }

    override fun updateCourseBasicDetails(courseId: Int, updateData: UpdateCourseBasicData): Result<Unit> {
        var sql = "UPDATE Course SET "
        if (updateData.title != null)
            sql += "title=${updateData.title},"
        if (updateData.description != null)
            sql += "description=${updateData.description},"
        if (updateData.skills != null)
            sql += "skills=${updateData.skills},"
        if (updateData.prerequisites != null)
            sql += "prerequisites=${updateData.prerequisites},"
        if (updateData.status != null)
            sql += "status=${updateData.status}"
    }

    override fun updateModuleDetails(moduleId: Int, updateData: UpdateModuleData): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateLessonDetails(lessonId: Int, updateData: UpdateLessonData): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateModuleDuration(moduleId: Int, duration: Int): Boolean {
        val sql = """UPDATE "Module" 
            |SET duration=$duration + (SELECT duration FROM "Module" WHERE id=$moduleId)
            |WHERE id=$moduleId""".trimMargin()

        conn.createStatement().use { stmt ->
            val count = stmt.executeUpdate(sql)
            println("count: $count")
            return count != 0
        }
    }

    override fun updateCourseDuration(courseId: Int, duration: Int): Boolean {
        val sql = """UPDATE Course 
            |SET duration=$duration + (SELECT duration FROM Course WHERE id=$courseId) 
            |WHERE id=$courseId""".trimMargin()

        conn.createStatement().use { stmt ->
            val count = stmt.executeUpdate(sql)
            println("count: $count")
            return count != 0
        }
    }

    // ******************* EXISTS *******************
    override fun isCategoryExists(name: String): Boolean {
        TODO("Not yet implemented")
    }

}