package core.user.schemas

import java.time.LocalDateTime

import java.util.UUID

abstract class BaseUser {
    abstract val id: UUID
    abstract val firstName: String
    abstract val lastName: String
    abstract val email: String
    abstract val role: UserRole
    abstract val status: UserStatus
    abstract val hashPassword: String
    abstract val lastLoginAt: LocalDateTime
    val fullName: String
        get() = "$firstName $lastName"
}

data class UserData (
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: UserRole,
    override val status: UserStatus,
    override val hashPassword: String,
    override val lastLoginAt: LocalDateTime,
) : BaseUser()

data class StudentData (
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: UserRole,
    override val status: UserStatus,
    override val hashPassword: String,
    override val lastLoginAt: LocalDateTime,
    val gitHubUrl: String? = null,
    val linkedInUrl: String? = null,
) : BaseUser()

data class TrainerData (
    override val id: UUID,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val role: UserRole,
    override val status: UserStatus,
    override val hashPassword: String,
    override val lastLoginAt: LocalDateTime,
    val educations: List<EducationData> = mutableListOf(),
    val workExperiences: List<WorkExperienceData> = mutableListOf(),
    val technicalSkills: List<String>,
    val softSkills: List<String> = mutableListOf(),
) : BaseUser()

data class EducationData (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    val institution: String,
    val degree: String,
    val startMonth: Int,
    val startYear: Int,
    val endMonth: Int?,
    val endYear: Int?,
    val isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
)

data class WorkExperienceData (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    val company: String,
    val designation: String,
    val startMonth: Int,
    val startYear: Int,
    val endMonth: Int?,
    val endYear: Int?,
    val isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
)

data class CurrentUserData (
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val status: UserStatus,
)