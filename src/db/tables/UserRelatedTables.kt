package db.tables

import java.time.LocalDateTime
import java.util.UUID

data class UserTable(
    val id : UUID, // PK
    var firstName: String,
    var lastName: String,
    var email: String,
    var role: UserRole,
    var hashPassword: String,
    var passwordChangedAt: LocalDateTime?,
    var lastLoginAt: LocalDateTime,
    var status: UserStatus,
) : Timeline () {
    companion object {
        val records = mutableMapOf<Int, UserTable>()
    }
}


data class StudentTable(
    var studentId: UUID, // Foreign Key from User Table
    var gitHubUrl: String?,
    var linkedInUrl: String?,
) : Timeline () {
    companion object {
        val records = mutableMapOf<Int, StudentTable>()
    }
}

data class EducationTable(
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var institution: String,
    var degree: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline() {
    companion object {
        val records = mutableMapOf<Int, EducationTable>()
    }
}

data class WorkExperienceTable (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var company: String,
    var designation: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline() {
    companion object {
        val records = mutableMapOf<Int, WorkExperienceTable>()
    }
}

data class TrainerTable (
    val trainerId: UUID, // Foreign Key from User Table
    var technicalSkills: List<String>,
    var softSkills: List<String>,
) : Timeline() {
    companion object {
        val records = mutableMapOf<Int, TrainerTable>()
    }
}