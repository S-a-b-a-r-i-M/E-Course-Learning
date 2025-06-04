package db.inmemorystore.course

import db.ResourceStatus
import db.Timeline

class Module (
    val id : Int,
    val sequenceNumber: Int,
    val title: String,
    val description: String?,
    var duration : Float, //note: "duration in minutes"
    val status: ResourceStatus,
    val lessonIds: List<Int> = mutableListOf<Int>(),
) : Timeline() {
    companion object {
        private val records = mutableMapOf<Int, Module>()
    }
}