package core.course.schemas

import java.util.UUID

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
    var duration: Int = 0,
    val courseLevel: CourseLevel,
    val courseType: CourseType,
    val status: ResourceStatus,
    val prerequisites: List<String>? = null,
    val priceDetails: PriceDetailsData? = null,
    val modules: List<ModuleData> = listOf(),
) {
    companion object {
        fun fromNewCourseBasicData(courseId: Int, createdBy: UUID, newCourseData: NewCourseBasicData) =
            DetailedCourseData(
                id = courseId,
                title = newCourseData.title,
                description = newCourseData.description,
                createdBy = createdBy,
                skills = newCourseData.skills,
                courseLevel = newCourseData.courseLevel,
                courseType = newCourseData.courseType,
                status = ResourceStatus.PUBLISHED,
                prerequisites = newCourseData.prerequisites,
                category = newCourseData.category
            )
    }
}

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
) {
    companion object {
        fun from(priceDetailsId: Int, newPriceData: NewPriceData) =
            PriceDetailsData(
                id = priceDetailsId,
                currencyCode = newPriceData.currencyCode,
                currencySymbol = newPriceData.currencySymbol,
                amount = newPriceData.amount
            )
    }
}

data class UpdatePriceDetailsData (
    var id: Int = 0,
    var currencyCode: String? = null,
    var currencySymbol: String? = null,
    var amount: Double? = null,
)

data class NewModuleData (
    val title: String,
    val description: String?,
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class ModuleData (
    val id: Int,
    val title: String,
    val description: String?,
    var duration: Int = 0,
    val status: ResourceStatus = ResourceStatus.PUBLISHED,
    val lessons: List<LessonData> = listOf(),
) {
    companion object {
        fun from(moduleId: Int, newModuleData: NewModuleData) =
            ModuleData(
                id = moduleId,
                title = newModuleData.title,
                description = newModuleData.description,
                status = newModuleData.status,
            )
    }
}

data class UpdateModuleData (
    var title: String? = null,
    var description: String? = null,
    var status: ResourceStatus? = null,
)

data class NewLessonData (
    val title: String,
    val resource: String,
    var duration: Int, // note: "duration in minutes"
    var status: ResourceStatus = ResourceStatus.PUBLISHED,
)

data class LessonData (
    val id: Int,
    val title: String,
    val resource: String,
    val duration: Int, // note: "duration in minutes"
    val status: ResourceStatus = ResourceStatus.PUBLISHED,
) {
    companion object {
        fun from(lessonId: Int, newLessonData: NewLessonData) =
            LessonData (
                id = lessonId,
                title = newLessonData.title,
                resource = newLessonData.resource,
                duration = newLessonData.duration,
                status = newLessonData.status,
            )
    }
}

data class UpdateLessonData (
    var title: String? = null,
    var resource: String? = null,
    var duration: Int? = null, // note: "duration in minutes"
    var status: ResourceStatus? = null,
)

data class CategoryData (
    val id: Int,
    val name: String
)