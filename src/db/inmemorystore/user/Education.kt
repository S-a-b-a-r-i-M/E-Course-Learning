package db.inmemorystore.user

import db.Timeline
import java.util.UUID

data class Education(
    val id: Int, // PK
    val trainerId: UUID, // Foreign Key from User Table
    private var institution: String,
    private var degree: String,
    private var startMonth: Int,
    private var startYear: Int,
    private var endMonth: Int?,
    private var endYear: Int?,
    private var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline() {

    fun getInstitution() = institution

    fun getDegree() = degree

    fun getStartMonth() = startMonth

    fun getStartYear() = startYear

    fun getEndMonth() = endMonth

    fun getEndYear() = endYear

    fun isCurrent() = isCurrent

    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Education>()
    }
}