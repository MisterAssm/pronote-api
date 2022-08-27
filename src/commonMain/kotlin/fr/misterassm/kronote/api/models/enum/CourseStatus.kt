package fr.misterassm.kronote.api.models.enum

enum class CourseStatus(val status: String) {

    NONE("None"),
    EDITED("Cours modifié"),
    REPLACEMENT("Remplacement"),
    CANCELLED("Cours annulé");

    fun isSpecial() = !equals(NONE)

    companion object {
        val statusMap = values().associateBy(CourseStatus::status)
    }

}