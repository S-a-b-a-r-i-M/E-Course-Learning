package db.inmemorystore.user

import db.UserRole
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

class Student (
    id : UUID, // PK
    firstName: String,
    lastName: String,
    email: String,
    role: UserRole,
    hashPassword: String,
    status: UserStatus,
    lastLoginAt: LocalDateTime,
    var gitHubUrl: String?,
    var linkedInUrl: String?,
    var interestedCategories: List<String> = mutableListOf<String>()
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