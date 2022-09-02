package fr.misterassm.kronote.api.models.enum

enum class PronoteGender(
    val title: String,
    val genderId: Int
) {

    GROUP_GENDER("Groupe", 2),
    TEACHER_GENDER("Professeur", 3),
    PAGE_EXPIRED("La page a expiré ! (1)", 8),
    SUBJECT_GENDER("Matière", 16),
    ROOM_GENDER("Salle", 17);

    companion object {
        fun Int.asPronoteGender() = values().firstOrNull { equals(it.genderId) }
    }

}