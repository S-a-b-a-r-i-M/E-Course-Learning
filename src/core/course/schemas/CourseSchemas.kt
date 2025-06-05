package core.course.schemas

import db.CourseLevel
import db.CourseType
import db.ResourceStatus

data class NewCourseBasicData (
    var title: String,
    var description: String,
    var skills: List<String>,
    var courseLevel: CourseLevel,
    var courseType: CourseType,
    var prerequisites: List<String>? = null,
    var isFreeCourse: Boolean,
    val priceData: NewPriceData? = null,
    var status: ResourceStatus = ResourceStatus.DRAFT,
    var categoryIds: List<Int> = listOf()
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