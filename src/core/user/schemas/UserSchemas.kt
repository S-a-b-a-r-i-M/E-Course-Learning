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
    abstract var status: UserStatus
}

data class UserData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    var hashedPassword: String,
) : BaseUserData()

data class StudentUserData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    var gitHubUrl: String?,
    var linkedInUrl: String?,
) : BaseUserData()

data class TrainerUserData (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
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