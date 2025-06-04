package db.inmemorystore.user

import db.Timeline
import db.UserRole
import db.UserStatus
import java.time.LocalDateTime
import java.util.UUID

open class User (
    val id : UUID, // PK
    var firstName: String,
    var lastName: String,
    val email: String,
    var role: UserRole,
    var hashPassword: String,
    var status: UserStatus,
    var lastLoginAt: LocalDateTime,
): Timeline() {
    companion object {
        private val records = mutableMapOf<UUID, User>()
        private val emailToIdMap = mutableMapOf<String, UUID>()

        fun create(user: User): Boolean {
            records[user.id] = user
            emailToIdMap[user.email] = user.id
            return true
        }

        fun update(userId: UUID, updateDataMap: Map<String, Any>): Boolean {
            val user = records[userId]
            if (user == null) return false

            if ("firstName" in updateDataMap.keys)
                user.firstName = updateDataMap["firstName"] as String
            if ("lastName" in updateDataMap.keys)
                user.lastName = updateDataMap["firstName"] as String
            if ("firstName" in updateDataMap.keys)
                user.status = updateDataMap["status"] as UserStatus
            if ("lastLoginAt" in updateDataMap.keys)
                user.lastLoginAt = updateDataMap["lastLoginAt"] as LocalDateTime

            return true
        }

        fun delete(){

        }

        fun getRecords(): Map<UUID, User> = records.toMap()

        fun getEmailToIdMap(): Map<String, UUID> = emailToIdMap.toMap()
    }
}