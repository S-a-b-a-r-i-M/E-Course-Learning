package core.course.schemas

import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import db.inmemorystore.course.Category
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.PriceDetails
import java.util.UUID

data class NewCourseBasicData (
    var title: String,
    var description: String,
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var isFreeCourse: Boolean,
    var categoryId: Int,
    var prerequisites: List<String>? = null,
    val priceData: NewPriceData? = null,
    var status: ResourceStatus = ResourceStatus.DRAFT,
)

// TODO: Needs to improve this to handle other field values
data class UpdateCourseBasicData (
    var title: String? = null,
    var description: String? = null,
    var skills: List<String>? = null,
    var courseLevel: CourseLevel? = null,
    var courseType: CourseType? = null,
    var status: ResourceStatus? = null,
    var prerequisites: List<String>? = null,
    var isFreeCourse: Boolean? = null,
    // TODO: Needs to add price details
)

data class CourseData(
    val id : Int,
    val title: String,
    val description: String,
    val duration : Float,
    val createdBy: UUID,
    val skills: List<String>,
    val courseLevel: CourseLevel,
    val courseType: CourseType,
    val isFreeCourse: Boolean,
    val status: ResourceStatus,
    val categoryId: Category,
    val prerequisites: List<String>? = null,
    val priceDetails: PriceDetails? = null,
    val modules: MutableList<ModuleData> = mutableListOf(),
)

data class NewPriceData (
    val currencyCode: String,
    val currencySymbol: String,
    var amount: Double,
)

data class NewModuleData (
    val title: String,
    val description: String?,
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class ModuleData(
    val id : Int,
    val title: String,
    val description: String?,
    val sequenceNumber: Int,
    val duration : Float = 0.0f,
    val status: ResourceStatus,
    val lessons: MutableList<Lesson> = mutableListOf(),
)

data class UpdateModuleData (
    val title: String? = null,
    val description: String? = null,
    val sequenceNumber: Int? = null,
    val status: ResourceStatus? = null,
)

data class NewLessonData (
    val title: String,
    val resource: String,
    var duration: Float, // note: "duration in minutes"
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)