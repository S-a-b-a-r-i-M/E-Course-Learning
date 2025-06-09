package core.course.schemas

import db.CourseLevel
import db.CourseType
import db.ResourceStatus
import db.inmemorystore.course.Course
import db.inmemorystore.course.Lesson
import db.inmemorystore.course.Module
import db.inmemorystore.course.PriceDetails

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

data class DetailedCourseData (
    val course: Course,
    var priceDetails: PriceDetails? = null,
    var modules: List<ModuleData>? = null
)

data class ModuleData (
    val module: Module,
    val lessons: List<Lesson>? = null,
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