package fr.misterassm.kronote.api.models

enum class Page(
    val kPageName: String,
    val id: Int
) {

    HOME("PageAccueil", 7),
    TIMETABLE("PageEmploiDuTemps", 16),

}
