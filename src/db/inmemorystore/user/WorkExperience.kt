package db.inmemorystore.user

import db.Timeline
import java.util.UUID

data class WorkExperience (
    private val id: Int, // PK
    private val trainerId: UUID, // Foreign Key from User Table
    private var company: String,
    private var designation: String,
    private var startMonth: Int,
    private var startYear: Int,
    private var endMonth: Int?,
    private var endYear: Int?,
    private var isCurrent: Boolean = false, // "by default false. If this is true, end dates will be null"
) : Timeline() {

    fun getId() = id

    fun getTrainerId() = trainerId

    fun getCompany() = company

    fun getDesignation() = designation

    fun getStartMonth() = startMonth

    fun getStartYear() = startYear

    fun getEndMonth() = endMonth

    fun getEndYear() = endYear

    fun isCurrent() = isCurrent

    companion object {
        val records = mutableMapOf<Int, WorkExperience>()
    }
}