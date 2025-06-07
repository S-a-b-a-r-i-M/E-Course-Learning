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
    private var gitHubUrl: String?,
    private var linkedInUrl: String?,
    private var interestedCategories: List<String> = mutableListOf<String>()
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

    fun getGitHubUrl() = gitHubUrl

    fun getLinkedInUrl() = linkedInUrl

    fun getInterestedCategories() = interestedCategories.toList() // returns a copy (immutable view)

    companion object {
        private val records = mutableMapOf<UUID, Student>()
    }
}