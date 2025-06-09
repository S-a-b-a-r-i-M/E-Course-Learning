package core.user.schemas

import db.UserRole
import db.UserStatus

import java.util.UUID

abstract class BaseUserData {
    abstract val userId: UUID
    abstract var firstName: String
    abstract var lastName: String
    abstract var email: String
    abstract var role: UserRole
    abstract val status: UserStatus
    abstract var hashedPassword: String
}

data class UserData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override val status: UserStatus,
    override var hashedPassword: String,
) : BaseUserData()

data class StudentData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override val status: UserStatus,
    override var hashedPassword: String,
    var gitHubUrl: String?,
    var linkedInUrl: String?,
) : BaseUserData()

data class TrainerData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override val status: UserStatus,
    override var hashedPassword: String,
    val educations: List<EducationData> = mutableListOf<EducationData>(),
    val workExperiences: List<WorkExperienceData> = mutableListOf<WorkExperienceData>(),
    var technicalSkills: List<String>,
    var softSkills: List<String> = mutableListOf<String>(),
) : BaseUserData()

data class EducationData (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var institution: String,
    var degree: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
)

data class WorkExperienceData (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var company: String,
    var designation: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
)