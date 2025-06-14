package core.course.schemas

import java.util.UUID

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

data class NewCourseBasicData (
    var title: String,
    var description: String,
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var category: String,
    var prerequisites: List<String>? = null,
    val priceData: NewPriceData? = null,
    var status: ResourceStatus = ResourceStatus.DRAFT,
)

data class CourseBasicData(
    val id : Int,
    val createdBy: UUID, // By which admin
    val category: String,
    var title: String,
    var description: String,
    var duration : Int, //note: "duration in minutes"
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var status: ResourceStatus,
    var prerequisites: List<String>? = null,
    var priceDetails: PriceDetailsData? = null,
    val moduleIds: List<Int>? = null,
)

data class DetailedCourseData (
    val id : Int,
    val createdBy: UUID, // By which admin
    val category: String?,
    val title: String,
    val description: String,
    val skills: List<String>,
    val courseLevel: CourseLevel,
    val courseType: CourseType,
    val status: ResourceStatus,
    val prerequisites: List<String>? = null,
    val priceDetails: PriceDetailsData? = null,
    val modules: MutableList<ModuleData> = mutableListOf(),
    val parentCourseId: Int? = null // Used to connect this course with its parent course
)

// TODO: Needs to improve this to handle other field values
data class UpdateCourseBasicData (
    var title: String? = null,
    var description: String? = null,
    var skills: List<String>? = null,
    var status: ResourceStatus? = null,
    var prerequisites: List<String>? = null,
)

data class NewPriceData (
    val currencyCode: String,
    val currencySymbol: String,
    var amount: Double,
)

data class PriceDetailsData (
    val id: Int,
    val currencyCode: String,
    val currencySymbol: String,
    val amount: Double,
)

data class UpdatePriceDetailsData (
    var id: Int = 0,
    var currencyCode: String? = null,
    var currencySymbol: String? = null,
    var amount: Double? = null,
)

data class NewModuleData (
    val title: String,
    val description: String?,
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class ModuleData (
    val id: Int,
    val title: String,
    val description: String?,
    val duration: Int = 0,
    val sequenceNumber: Int = 0,
    val status: ResourceStatus = ResourceStatus.PUBLISHED,
    val lessons: MutableList<LessonData> = mutableListOf(),
)

data class UpdateModuleData (
    var title: String? = null,
    var description: String? = null,
//    var sequenceNumber: Int? = null,
    var status: ResourceStatus? = null,
)

data class NewLessonData (
    val title: String,
    val resource: String,
    var duration: Int, // note: "duration in minutes"
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class LessonData (
    val id: Int,
    val title: String,
    val resource: String,
    val duration: Int, // note: "duration in minutes"
    val sequenceNumber: Int = 0,
    val status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class UpdateLessonData (
    var title: String? = null,
    var resource: String? = null,
    var duration: Int? = null, // note: "duration in minutes"
//    var sequenceNumber: Int = 0,
    var status: ResourceStatus? = null,
)

data class CategoryData (
    val id: Int,
    val name: String
)