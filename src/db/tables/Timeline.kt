package db.tables

import java.time.LocalDateTime

open class Timeline {
    val createdAt: LocalDateTime = LocalDateTime.now()
    var modifiedAt: LocalDateTime = LocalDateTime.now()
}