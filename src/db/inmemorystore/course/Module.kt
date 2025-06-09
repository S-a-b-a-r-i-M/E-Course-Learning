package db.inmemorystore.course

import core.course.schemas.NewModuleData
import core.course.schemas.UpdateModuleData
import db.ResourceStatus
import db.Timeline

class Module (
    val id : Int,
    private var title: String,
    private var description: String?,
    private var sequenceNumber: Int,
    private var duration : Float = 0.0f, //note: "duration in minutes"
    private var status: ResourceStatus,
    private val lessonIds: MutableList<Int> = mutableListOf(),
) : Timeline() {
    fun getTitle(): String = title

    fun getDescription(): String? = description

    fun getSequenceNumber(): Int = sequenceNumber

    fun getDuration(): Float = duration

    fun getStatus(): ResourceStatus = status

    fun getLessonIds(): List<Int> = lessonIds.toList() // returns a copy (immutable view)

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

        fun updateModule(moduleId: Int, updateData: UpdateModuleData): Boolean {
            val module = records.getValue(moduleId)
            updateData.title?.let { module.title = it }
            updateData.description?.let { module.description = it }
            updateData.sequenceNumber?.let { module.sequenceNumber = it } // TODO: think this logic
            updateData.status?.let { module.status = it }
            return true
        }

        fun updateDuration(moduleId: Int, duration: Float): Float {
            val module = records.getValue(moduleId)
            module.duration += duration // Addition of duration
            return module.duration
        }

        fun addLessonId(moduleId: Int, lessonId: Int) {
            records.getValue(moduleId).lessonIds.add(lessonId)
        }

        fun getRecords() = records.toMap()
    }
}