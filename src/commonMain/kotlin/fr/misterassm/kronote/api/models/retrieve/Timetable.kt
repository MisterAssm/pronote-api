package fr.misterassm.kronote.api.models.retrieve

import fr.misterassm.kronote.api.adapter.RetrieveAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Timetable(
    @SerialName("avecCoursAnnule") val withCancelledCourses: Boolean,
    @SerialName("ListeCours") var courseList: List<Course>,
    @SerialName("AvecTafPublie") val workPublished: Boolean = false,
    @SerialName("cahierDeTextes") val attachedHomework: JsonElement? = null, // TODO: Not yet implemented
    var weekNumber: Int = 0
    // TODO: Absences ?
    // TODO: recreations ?
) : RetrieveAdapter