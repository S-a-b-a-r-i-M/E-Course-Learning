package db.inmemorystore.user

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
    private val educationIds: List<Int> = mutableListOf(),
    private val workExperienceIds: List<Int> = mutableListOf()
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

    fun getEducationIds() = educationIds.toList() // returns a copy (immutable view)

    fun getWorkExperienceIds() = workExperienceIds.toList()

    companion object {
        private var serialId = 1
        private val records = mutableMapOf<UUID, Student>()
    }
}