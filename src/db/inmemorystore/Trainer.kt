package db.inmemorystore

import db.Timeline
import db.UserRole
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

class Trainer (
    id : UUID, // PK
    firstName: String,
    lastName: String,
    email: String,
    role: UserRole,
    hashPassword: String,
    status: UserStatus,
    lastLoginAt: LocalDateTime,
    educationIds: List<Int> = mutableListOf<Int>(),
    workExperienceIds: List<Int> = mutableListOf<Int>()
) : User(
    id,
    firstName,
    lastName,
    email,
    role,
    hashPassword,
    status,
    lastLoginAt,
) {
    companion object {
        private val records = mutableMapOf<UUID, Student>()
    }
}

data class Education(
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
        val records = mutableMapOf<Int, Education>()
    }
}

data class WorkExperience (
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
        val records = mutableMapOf<Int, WorkExperience>()
    }
}