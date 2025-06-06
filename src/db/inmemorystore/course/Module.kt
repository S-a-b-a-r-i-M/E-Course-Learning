package db.inmemorystore.course

import core.course.schemas.NewModuleData
import db.ResourceStatus
import db.Timeline
import javax.management.modelmbean.ModelMBean

class Module (
    val id : Int,
    val title: String,
    val description: String?,
    val sequenceNumber: Int,
    var duration : Float = 0.0f, //note: "duration in minutes"
    val status: ResourceStatus,
    val lessonIds: MutableList<Int> = mutableListOf(),
) : Timeline() {
    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, Module>()

        fun createModule(newModuleData: NewModuleData): Module {
            val module = Module (
                id = serialId++,
                title = newModuleData.title,
                description = newModuleData.description,
                sequenceNumber = newModuleData.sequenceNumber,
                status = newModuleData.status,
            )

            records[module.id] = module
            println("Module.kt: New module added(id-${module.id})")
            return module
        }

        fun addLessonId(moduleId: Int, lessonId: Int) {
            records.getValue(moduleId).lessonIds.add(lessonId)
        }
    }
}