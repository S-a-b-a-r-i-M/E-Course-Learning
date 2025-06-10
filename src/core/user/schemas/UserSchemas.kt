package core.user.schemas

import java.time.LocalDateTime

import java.util.UUID

abstract class BaseUser {
    abstract val id: UUID
    abstract var firstName: String
    abstract var lastName: String
    abstract var email: String
    abstract val role: UserRole
    abstract var status: UserStatus
    abstract val hashPassword: String
    abstract var lastLoginAt: LocalDateTime
}

data class UserData (
    override val id: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override val role: UserRole,
    override var status: UserStatus,
    override val hashPassword: String,
    override var lastLoginAt: LocalDateTime,
) : BaseUser()

data class StudentData (
    override val id: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    override var hashPassword: String,
    override var lastLoginAt: LocalDateTime,
    var gitHubUrl: String?,
    var linkedInUrl: String?,
) : BaseUser()

data class TrainerData (
    override val id: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    override var hashPassword: String,
    override var lastLoginAt: LocalDateTime,
    val educations: List<EducationData> = mutableListOf(),
    val workExperiences: List<WorkExperienceData> = mutableListOf(),
    var technicalSkills: List<String>,
    var softSkills: List<String> = mutableListOf(),
) : BaseUser()

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