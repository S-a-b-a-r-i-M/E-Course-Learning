package core.course.schemas

import java.util.UUID

val CURRENT_FILE_NAME: String? = Throwable().stackTrace[0].fileName

data class NewCourseBasicData (
    var title: String,
    var description: String,
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var isFreeCourse: Boolean,
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
    var isFreeCourse: Boolean,
    var status: ResourceStatus,
    var prerequisites: List<String>? = null,
    var priceDetails: PriceDetailsData? = null,
    val moduleIds: List<Int>? = null,
)

data class DetailedCourseData (
    val id : Int,
    val createdBy: UUID, // By which admin
    val category: String?,
    var title: String,
    var description: String,
    var duration : Int, //note: "duration in minutes"
    var skills: List<String>,
    var courseLevel: CourseLevel,
    val courseType: CourseType,
    var isFreeCourse: Boolean,
    var status: ResourceStatus,
    var prerequisites: List<String>? = null,
    var priceDetails: PriceDetailsData? = null,
    val modules: MutableList<ModuleData> = mutableListOf(),
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

data class NewPriceData (
    val currencyCode: String,
    val currencySymbol: String,
    var amount: Double,
)

data class PriceDetailsData (
    val id: Int,
    var currencyCode: String,
    var currencySymbol: String,
    var amount: Double,
)

data class NewModuleData (
    val title: String,
    val description: String?,
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class ModuleData (
    val id: Int,
    var title: String,
    var description: String?,
    var duration: Int = 0,
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
    val lessons: MutableList<LessonData> = mutableListOf(),
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
    var duration: Int, // note: "duration in minutes"
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class LessonData (
    val id: Int,
    var title: String,
    var resource: String,
    var duration: Int, // note: "duration in minutes"
    var sequenceNumber: Int = 0,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class CategoryData (
    val id: Int,
    val name: String
)