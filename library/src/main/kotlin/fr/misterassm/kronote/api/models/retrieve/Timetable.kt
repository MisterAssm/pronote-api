package fr.misterassm.kronote.api.models.retrieve

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Timetable(
    @SerialName("avecCoursAnnule") val withCancelledCourses: Boolean,
    @SerialName("ListeCours") val courseList: List<Course>,
    @SerialName("AvecTafPublie") val workPublished: Boolean = false,
    @SerialName("cahierDeTextes") val attachedHomework: JsonElement? = null // TODO: Not yet implemented
    // TODO: Absences ?
    // TODO: recreations ?
)
