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