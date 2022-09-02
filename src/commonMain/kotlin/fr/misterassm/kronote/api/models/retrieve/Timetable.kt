package fr.misterassm.kronote.api.models.retrieve

import fr.misterassm.kronote.api.adapter.RetrieveAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*
    Need to implement attachedHomework
    Also check for Absences & recreations
 */
@Serializable
data class Timetable(
    @SerialName("avecCoursAnnule") val withCancelledCourses: Boolean,
    @SerialName("ListeCours") var courseList: List<Course>,
    @SerialName("AvecTafPublie") val workPublished: Boolean = false,
    @SerialName("cahierDeTextes") val attachedHomework: JsonElement? = null,
    var weekNumber: Int = 0
) : RetrieveAdapter