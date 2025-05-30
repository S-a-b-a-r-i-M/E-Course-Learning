package core.user.models

import db.tables.Timeline
import db.tables.UserRole
import db.tables.UserStatus

import java.time.LocalDateTime
import java.util.UUID

/*
Why I choose 'sealed class',
1. Nobody shouldn't create object of my base user (opt: abstract, interface, sealed)
2. Exhaustive when expression (opt: sealed)
3. Apart from this file/package BaseUser shouldn't extend any other types (opt: sealed)
*/

sealed class BaseUser {
    abstract val userId: UUID
    abstract var firstName: String
    abstract var lastName: String
    abstract var email: String
    abstract var role: UserRole
    abstract var status: UserStatus
    val createdAt: LocalDateTime = LocalDateTime.now()
    var modifiedAt: LocalDateTime = LocalDateTime.now()
}

data class UserModel(
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
) : BaseUser()

data class StudentUserModel(
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    var gitHubUrl: String?,
    var linkedInUrl: String?,
) : BaseUser()

data class TrainerUserModel (
    override val userId: UUID,
    override var firstName: String,
    override var lastName: String,
    override var email: String,
    override var role: UserRole,
    override var status: UserStatus,
    val educations: List<EducationModel> = mutableListOf<EducationModel>(),
    val workExperiences: List<WorkExperienceModel> = mutableListOf<WorkExperienceModel>(),
    var technicalSkills: List<String>,
    var softSkills: List<String> = mutableListOf<String>(),
) : BaseUser()

data class EducationModel(
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var institution: String,
    var degree: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline()

data class WorkExperienceModel (
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    var company: String,
    var designation: String,
    var startMonth: Int,
    var startYear: Int,
    var endMonth: Int?,
    var endYear: Int?,
    var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline()

class NewUserModel (
    val firstName: String,
    val lastName: String,
    val email: String,
    val hashedPassword: String,
    val role: UserRole,
)