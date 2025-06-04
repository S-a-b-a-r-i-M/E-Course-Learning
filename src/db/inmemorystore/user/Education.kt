package db.inmemorystore.user

import db.Timeline
import java.util.UUID

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