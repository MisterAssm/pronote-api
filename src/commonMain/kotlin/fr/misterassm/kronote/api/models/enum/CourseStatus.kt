package fr.misterassm.kronote.api.models.enum

enum class CourseStatus(val status: String) {

    NONE("None"),
    EDITED("Cours modifié"),
    REPLACEMENT("Remplacement"),
    CANCELLED("Cours annulé");

    companion object {
        val statusMap = values().associateBy(CourseStatus::status)
    }

}