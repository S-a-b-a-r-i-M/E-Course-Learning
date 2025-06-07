package db

import java.time.LocalDateTime

open class Timeline {
    protected val createdAt: LocalDateTime = LocalDateTime.now()
    protected var modifiedAt: LocalDateTime = LocalDateTime.now()

    fun getCreatedAtDT() = createdAt

    fun getModifiedAtDT() = modifiedAt
}